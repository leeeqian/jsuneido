/* Copyright 2010 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.immudb;

import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.annotation.concurrent.NotThreadSafe;

import suneido.immudb.DbHashTrie.Entry;
import suneido.immudb.DbHashTrie.IntEntry;
import suneido.immudb.DbHashTrie.StoredIntEntry;
import suneido.immudb.DbHashTrie.Translator;

/**
 * Transaction "context". Manages IntRefs and Redirects and Storage.
 */
@NotThreadSafe
class Tran implements Translator {
	private static final int SIZEOF_INT = 4;
	static final int HEAD_SIZE = 2 * SIZEOF_INT; // size and datetime
	static final int TAIL_SIZE = 2 * SIZEOF_INT; // checksum and size
	{ assert TAIL_SIZE == MmapFile.align(TAIL_SIZE); }
	final Storage stor;
	private final Redirects redirs;
	final IntRefs intrefs = new IntRefs();
	private int head_adr = 0;

	Tran(Storage stor) {
		this.stor = stor;
		redirs = new Redirects(DbHashTrie.empty(stor));
	}

	Tran(Storage stor, int redirs) {
		this.stor = stor;
		this.redirs = new Redirects(DbHashTrie.from(stor, redirs));
	}

	Tran(Storage stor, Redirects redirs) {
		this.stor = stor;
		this.redirs = redirs;
	}

	int refToInt(Object ref) {
		return intrefs.refToInt(ref);
	}

	Object intToRef(int intref) {
		return intrefs.intToRef(intref);
	}

	int redir(int from) {
		return redirs.get(from);
	}

	void redir(int from, Object ref) {
		assert(! (ref instanceof Number));
		if (IntRefs.isIntRef(from))
			intrefs.update(from, ref);
		else
			redirs.put(from, refToInt(ref));
	}

	Redirects redirs() {
		return redirs;
	}

	void startStore() {
		intrefs.startStore();
		if (head_adr == 0)
			allowStore();
	}

	void allowStore() {
		stor.protect(); // enable output
		head_adr = stor.alloc(HEAD_SIZE); // to hold size and datetime
	}

	/**
	 * Store the size and date/time at the beginning of the commit (head)
	 * and the checksum and size at the end (tail).
	 * The checksum includes the head and a zero tail.
	 * The size includes the head and the tail
	 */
	void endStore() {
		int tail_adr = stor.alloc(TAIL_SIZE);
		int size = (int) stor.sizeFrom(head_adr);
		stor.buffer(head_adr).putInt(size).putInt(datetime());

		int cksum = checksum();
		stor.buffer(tail_adr).putInt(cksum).putInt(size);
		stor.protectAll(); // can't output outside tran
	}

	/**
	 * Returns the current time in seconds since Jan. 1, 1970 UTC
	 * Only good till 2038
	 */
	private static int datetime() {
		return (int) (System.currentTimeMillis() / 1000);
	}

	int checksum() {
		Checksum cksum = new Checksum();
		Iterator<ByteBuffer> iter = stor.iterator(head_adr);
		while (iter.hasNext())
			cksum.update(iter.next());
		return cksum.getValue();
	}

	void setAdr(int intref, int adr) {
		intrefs.setAdr(intref, adr);
	}

	int getAdr(int intref) {
		return intrefs.getAdr(intref);
	}

	int storeRedirs() {
		return redirs.store(this);
	}

	@Override
	public Entry translate(Entry e) {
		IntEntry ie = (IntEntry) e;
		int key = ie.key;
		int val = ie.value;
		if (IntRefs.isIntRef(val))
			val = getAdr(val);
		return new StoredIntEntry(key, val);
	}

	Record getrec(int adr) {
		if (IntRefs.isIntRef(adr))
			return (Record) intrefs.intToRef(adr);
		return new Record(stor, adr);
	}

	void mergeRedirs(DbHashTrie current) {
		redirs.merge(current);
	}

	public void assertNoRedirChanges(DbHashTrie current) {
		redirs.assertNoChanges(current);
	}

}
