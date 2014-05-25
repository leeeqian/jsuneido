/* Copyright 2012 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.immudb;

import com.google.common.primitives.Ints;

/**
 * Minimal reverse iterator.
 * @see StorageIter
 */
public class StorageIterReverse {
	private static final int MIN_SIZE = Tran.HEAD_SIZE + Tran.TAIL_SIZE;
	private final Storage stor;
	private final long fileSize;
	private long rpos = 0;

	StorageIterReverse(Storage stor) {
		this.stor = stor;
		fileSize = stor.sizeFrom(0);
	}

	boolean hasPrev() {
		return fileSize + rpos > MIN_SIZE;
	}

	int prev() {
		long size;
		while (true) {
			size = Storage.intToSize(stor.rbuffer(rpos - Ints.BYTES).getInt());
			if (size != 0)
				break;
			rpos -= Ints.BYTES;
		}
		if (! isValidSize(stor, rpos, size))
			throw new RuntimeException("bad size " + size);
		rpos -= size;
		return stor.rposToAdr(rpos);
	}

	private static boolean isValidSize(Storage stor, long pos, long size) {
		return MIN_SIZE <= size && stor.isValidPos(pos - size);
	}

}
