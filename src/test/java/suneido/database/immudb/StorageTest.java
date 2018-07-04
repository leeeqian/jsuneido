/* Copyright 2012 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.database.immudb;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StorageTest {
	private final Storage stor = new HeapStorage(64);

	@Test
	public void test_advance() {
		int adr1 = stor.alloc(40);
		stor.alloc(40); // in second chunk
		long size = stor.sizeFrom(adr1);
		int adr3 = stor.alloc(4);
		stor.buffer(adr3).putInt(12345678);
		int adr3b = stor.advance(adr1, size);
		assertEquals(adr3, adr3b);
		assertEquals(12345678, stor.buffer(adr3b).getInt());
	}

	@Test
	public void test_grow() {
		for (int i = 0; i < stor.INIT_CHUNKS * 3; ++i)
			stor.buffer(stor.alloc(40)).putInt(12345678);
	}

}
