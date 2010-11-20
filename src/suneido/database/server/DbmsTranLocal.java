package suneido.database.server;

import suneido.SuException;
import suneido.database.*;
import suneido.database.query.*;
import suneido.database.query.Query.Dir;
import suneido.database.server.Dbms.HeaderAndRow;

public class DbmsTranLocal implements DbmsTran {
	final Transaction t;

	public DbmsTranLocal(boolean readwrite) {
		t = readwrite ? TheDb.db().readwriteTran() : TheDb.db().readonlyTran();
	}

	public DbmsTranLocal(Transaction t) {
		this.t = t;
	}

	@Override
	public String complete() {
		return t.complete();
	}

	@Override
	public void abort() {
		t.abort();
	}

	@Override
	public int request(String s) {
		Query q = CompileQuery.parse(t, ServerData.forThread(), s);
		return ((QueryAction) q).execute();
	}

	@Override
	public DbmsQuery query(String s) {
		//System.out.println("\t" + s);
		return new DbmsQueryLocal(CompileQuery.query(t, ServerData.forThread(), s));
	}

	@Override
	public HeaderAndRow get(Dir dir, String query, boolean one) {
		Query q = CompileQuery.query(t, ServerData.forThread(), query);
		Row row = q.get(dir);
		if (row == null)
			return null;
		if (q.updateable())
			row.recadr = row.getFirstData().off();
		if (one && q.get(dir) != null)
			throw new SuException("Query1 not unique: " + query);
		Header hdr = q.header();
		return new HeaderAndRow(hdr, row);
	}

	@Override
	public void erase(long recadr) {
		t.removeRecord(recadr);
	}

	@Override
	public long update(long recadr, Record rec) {
		return t.updateRecord(recadr, rec);
	}

	@Override
	public boolean isReadonly() {
		return t.isReadonly();
	}

	@Override
	public boolean isEnded() {
		return t.isEnded();
	}

}