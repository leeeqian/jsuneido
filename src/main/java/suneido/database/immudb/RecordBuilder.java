/* Copyright 2011 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.database.immudb;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import gnu.trove.list.array.TIntArrayList;
import suneido.runtime.Pack;

public class RecordBuilder {
	private final ArrayList<ByteBuffer> bufs = new ArrayList<>();
	private final TIntArrayList offs = new TIntArrayList();
	private final TIntArrayList lens = new TIntArrayList();

	/** add a field of the record */
	RecordBuilder add(Record r, int i) {
		if (i < r.size())
			add1(r.fieldBuffer(i), r.fieldOffset(i), r.fieldLength(i));
		else
			addMin();
		return this;
	}

	/** add a prefix of the fields of the record */
	RecordBuilder addPrefix(Record rec, int prefixLength) {
		for (int i = 0; i < prefixLength; ++i)
			add(rec, i);
		return this;
	}

	RecordBuilder addFields(Record rec, int... fields) {
		for (int f : fields)
			add(rec, f);
		return this;
	}

	public RecordBuilder add(long n) {
		ByteBuffer buf = Pack.packLong(n);
		add1(buf, 0, buf.remaining());
		return this;
	}

	public RecordBuilder add(Object x) {
		ByteBuffer buf = Pack.pack(x);
		add1(buf, 0, buf.remaining());
		return this;
	}

	public RecordBuilder addAll(Record rec) {
		Record r = rec;
		for (int i = 0; i < r.size(); ++i)
			add1(r.fieldBuffer(i), r.fieldOffset(i), r.fieldLength(i));
		return this;
	}

	public RecordBuilder add(ByteBuffer buf) {
		add1(buf, buf.position(), buf.remaining());
		return this;
	}

	public RecordBuilder addMin() {
		return add(Record.MIN_FIELD);
	}

	public RecordBuilder addMax() {
		return add(Record.MAX_FIELD);
	}

	private void add1(ByteBuffer buf, int off, int len) {
		bufs.add(buf);
		offs.add(off);
		lens.add(len);
	}

	public RecordBuilder truncate(int n) {
		for (int i = bufs.size() - 1; i >= n; --i) {
			bufs.remove(i);
			offs.removeAt(i);
			lens.removeAt(i);
		}
		return this;
	}

	public RecordBuilder trim() {
		int n = bufs.size();
		while (n >= 1 && lens.get(n - 1) == 0)
				--n;
		return truncate(n);
	}

	int size() {
		return bufs.size();
	}

	public DataRecord build() {
		return arrayRec().dataRecord();
	}

	BtreeKey btreeKey(int dataAdr) {
		return new BtreeKey(bufRec(), dataAdr);
	}

	BtreeTreeKey btreeTreeKey(int dataAdr, int childAdr) {
		return new BtreeTreeKey(bufRec(), dataAdr, childAdr);
	}

	BtreeTreeKey btreeTreeKey(BtreeNode child) {
		return new BtreeTreeKey(bufRec(), 0, 0, child);
	}

	BufRecord bufRec() {
		return arrayRec().bufRecord();
	}

	ArrayRecord arrayRec() {
		return new ArrayRecord(bufs, offs, lens);
	}

}
