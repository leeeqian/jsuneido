package suneido.database;

import java.util.*;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.*;

/** Synchronized by {@link Transactions} which is the only user */
@NotThreadSafe
public class Locks {

	private final SetMultimap<Transaction, Long> locksRead = HashMultimap.create();
	private final SetMultimap<Transaction, Long> locksWrite = HashMultimap.create();
	private final SetMultimap<Long, Transaction> readLocks = HashMultimap.create();
	private final Map<Long, Transaction> writeLocks = new HashMap<Long, Transaction>();
	private final SetMultimap<Long, Transaction> writes = HashMultimap.create();

	/** @return A transaction if it has a write lock on this adr,
	 * otherwise null
	 */
	public Transaction addRead(Transaction tran, long adr) {
		if (tran.isEnded())
			return null;
		Transaction prev = writeLocks.get(adr);
		if (prev == tran)
			return null; // don't need readLock if already have writeLock
		readLocks.put(adr, tran);
		locksRead.put(tran, adr);
		return prev;
	}

	/** @return null if another transaction already has write lock,
	 *  or else a set (possibly empty) of other transactions that have read locks.
	 *  NOTE: The set may contain the locking transaction.
	 */
	public ImmutableSet<Transaction> addWrite(Transaction tran, long adr) {
		if (tran.isEnded())
			return null;
		Transaction prev = writeLocks.get(adr);
		if (prev == tran)
			return ImmutableSet.of(); // already have write lock
		if (prev != null)
			return null; // write-write conflict
		for (Transaction w : writes.get(adr)) {
			if (w != tran && !w.committedBefore(tran))
				return null; // write-write conflict with completed
		}
		writeLocks.put(adr, tran);
		writes.put(adr, tran);
		readLocks.remove(adr, tran); // read lock not needed when write lock
		locksWrite.put(tran, adr);
		return ImmutableSet.copyOf(readLocks.get(adr));
	}

	/** Just remove writeLocks. Other locks kept till finalization */
	public void commit(Transaction tran) {
		for (Long adr : locksWrite.get(tran))
			writeLocks.remove(adr);
	}

	/** Remove all locks for this transaction */
	public void remove(Transaction tran) {
		for (Long adr : locksRead.get(tran))
			readLocks.remove(adr, tran);
		locksRead.removeAll(tran);
		assert !locksRead.isEmpty() || readLocks.isEmpty();

		for (Long adr : locksWrite.get(tran)) {
			writeLocks.remove(adr);
			writes.remove(adr, tran);
		}
		locksWrite.removeAll(tran);
		assert !locksWrite.isEmpty() || (writeLocks.isEmpty() && writes.isEmpty());
	}

	public Set<Transaction> writes(long offset) {
		return ImmutableSet.copyOf(writes.get(offset));
	}

	public boolean isEmpty() {
		return locksRead.isEmpty() && locksWrite.isEmpty();
	}

	public void checkEmpty() {
		assert readLocks.isEmpty();
		assert writeLocks.isEmpty();
		assert writes.isEmpty();
		assert locksRead.isEmpty();
		assert locksWrite.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Locks");
		for (Map.Entry<Long, Transaction> e : writeLocks.entrySet())
			sb.append(" " + e.getValue().num + "w" + e.getKey());
		for (Map.Entry<Long, Transaction> e : readLocks.entries())
			sb.append(" " + e.getValue().num + "r" + e.getKey());
		return sb.toString();
	}

}
