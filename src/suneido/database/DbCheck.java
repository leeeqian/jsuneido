/* Copyright 2008 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.database;

import static suneido.intfc.database.DatabasePackage.nullObserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import suneido.database.Database.TN;
import suneido.intfc.database.DatabasePackage.Observer;
import suneido.intfc.database.DatabasePackage.Status;
import suneido.util.ByteBuf;
import suneido.util.Checksum;

/**
 * check the consistency of a database
 * e.g. after finding it was not shutdown properly
 */
class DbCheck {
	private final String dbFilename;
	final Mmfile mmf;
	long last_good_commit = 0; // offset
	String details = "";
	protected final Observer ob;

	static Status check(String dbFilename) {
		return check(dbFilename, nullObserver);
	}

	static Status check(String dbFilename, Observer ob) {
		return new DbCheck(dbFilename, ob).check();
	}

	DbCheck(String dbFilename, Observer ob) {
		this.dbFilename = dbFilename;
		this.ob = ob;
		mmf = new Mmfile(dbFilename, Mode.READ_ONLY);
	}

	Status check() {
		println("Checking commits and shutdowns");
		Status status = check_commits_and_shutdowns();
		if (status == Status.OK) {
			println("Checking data and indexes");
			if (!check_data_and_indexes())
				status = Status.CORRUPTED;
		}
		print(details);
		println((dbFilename.endsWith(".tmp") ? "" : dbFilename + " ") +
				status + " " + lastCommit(status));
		return status;
	}

	String lastCommit(Status status) {
		Date d = new Date(last_good_commit);
		return status == Status.UNRECOVERABLE
			? "Unrecoverable"
			: "Last " + (status == Status.CORRUPTED ? "good " : "")	+ "commit "
					+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
	}

	private Status check_commits_and_shutdowns() {
		if (mmf.first() == 0) {
			details = "no data\n";
			return Status.UNRECOVERABLE;
		}
		boolean ok = false;
		boolean has_a_shutdown = false;
		Checksum cksum = new Checksum();

		Mmfile.Iter iter = mmf.iterator();
		loop: while (iter.next()) {
			ok = false;
			ByteBuf buf = iter.current();
			switch (iter.type()) {
			case Mmfile.DATA:
				int tblnum = buf.getInt(0);
				if (tblnum != TN.TABLES && tblnum != TN.INDEXES) {
					Record r = new Record(buf.slice(4));
					cksum.update(buf.getByteBuffer(), r.bufSize() + 4);
					// + 4 to skip tblnum
				}
				break;
			case Mmfile.COMMIT:
				Commit commit = new Commit(buf);
				cksum.update(buf.getByteBuffer(), commit.sizeWithoutChecksum());
				if (commit.getChecksum() != cksum.getValue()) {
					details += "checksum mismatch\n";
					break loop;
				}
				last_good_commit = commit.getDate();
				cksum.reset();
				process_deletes(commit);
				break;
			case Mmfile.SESSION:
				if (buf.get(0) == Session.SHUTDOWN) {
					has_a_shutdown = true;
					ok = true;
				}
				break;
			case Mmfile.OTHER:
				// ignore
				break;
			default:
				details += "invalid block type\n";
				break loop;
			}
		}
		if (! has_a_shutdown) {
			details += "no valid shutdowns\n";
			return Status.UNRECOVERABLE;
		}
		if (last_good_commit == 0) {
			details += "no valid commits\n";
			return Status.UNRECOVERABLE;
		}
		if (iter.corrupt()) {
			details += "iteration failed\n";
			return Status.CORRUPTED;
		}
		if (!ok) {
			details += "missing last shutdown\n";
			return Status.CORRUPTED;
		}
		return Status.OK;
	}

	protected void process_deletes(Commit commit) {
		// empty stub, overridden by DbRebuild
	}

	private static final int BAD_LIMIT = 10;
	private static final int N_THREADS = 8;

	protected boolean check_data_and_indexes() {
		ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
		ExecutorCompletionService<String> ecs = new ExecutorCompletionService<String>(executor);
		Database db = Database.openReadonly(dbFilename);
		Transaction t = db.readonlyTran();
		try {
			BtreeIndex bti = t.getBtreeIndex(Database.TN.TABLES, "tablename");
			BtreeIndex.Iter iter = bti.iter();
			iter.next();
			int ntables = 0;
			for (; !iter.eof(); iter.next()) {
				Record r = t.input(iter.keyadr());
				String tablename = r.getString(Table.TABLE);
				ecs.submit(new CheckTable(db, tablename));
				++ntables;
			}
			int nbad = 0;
			for (int i = 0; i < ntables; ++i) {
				String errors;
				try {
					errors = ecs.take().get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					errors = "checkTable interrruped\n";
				} catch (ExecutionException e) {
					errors = "checkTable " + e;
				}
				if (! errors.isEmpty()) {
					details += errors;
					if (++nbad > BAD_LIMIT) {
						executor.shutdownNow();
						details += "TOO MANY ERRORS, GIVING UP\n";
						break;
					}
				}
			}
			return nbad == 0;
		} catch (Throwable e) {
			details += e + "\n";
			return false;
		} finally {
			executor.shutdown();
			t.complete();
			db.close();
		}
	}

	private void print(String s) {
		ob.print(s);
	}
	private void println() {
		ob.print("\n");
	}
	private void println(String s) {
		print(s);
		println();
	}

	public static void main(String[] args) {
		check("suneido.db", suneido.intfc.database.DatabasePackage.printObserver);
	}

}
