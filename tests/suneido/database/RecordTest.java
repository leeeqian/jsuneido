package suneido.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static suneido.database.Record.MINREC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import suneido.language.Ops;
import suneido.language.Pack;
import suneido.util.ByteBuf;

import com.google.common.collect.ImmutableList;

public class RecordTest {
	static final String data = "abc";
	static final String data2 = "x";

	@Test
	public void grow() {
		Record r = new Record();
		r.add(data);
		assertEquals(1, r.size());
		assertEquals(4, r.fieldSize(0));
		assertEquals(data, r.getString(0));

		assertTrue(Record.MINREC.isEmpty());
	}

	@Test
	public void test() {
		for (int sz : new int[] { 100, 1000, 100000 }) {
			Record r = new Record(sz);
			assertEquals(sz, r.bufSize());
			assertEquals(0, r.size());

			assertEquals(0, r.fieldSize(0));
			ByteBuffer bb = r.getRaw(0);
			assertEquals(0, bb.limit());
			assertEquals(0, r.getRaw(0).limit());

			r.add(data);
			assertEquals(1, r.size());
			assertEquals(4, r.fieldSize(0));
			bb = r.getRaw(0);
			assertEquals(4, bb.remaining());
			assertEquals(data, Ops.toStr(Pack.unpack(bb)));
			assertEquals(data, r.getString(0));

			assertEquals(4, r.fieldSize(0));
			assertEquals(data, r.getString(0));
		}
	}

	@Test
	public void bufsize() {
		assertEquals(5, Record.packSize(0, 0));
		assertEquals(11, Record.packSize(1, 5));
		assertEquals(1206, Record.packSize(100, 1000));
		assertEquals(104008, Record.packSize(1000, 100000));
	}

	@Test
	public void addPackable() {
		Record r = new Record();
		String s = String.valueOf("hello");
		r.add(s);
		assertEquals(s, r.get(0));
		String s2 = String.valueOf("world");
		r.add(s2);
		assertEquals(s, r.get(0));
		assertEquals(s2, r.get(1));

	}

	@Test
	public void packBufRecord() {
		Record r = new Record(1000);
		assertEquals(1000, r.bufSize());
		assertEquals(5, r.packSize());

		ByteBuffer buf = ByteBuffer.allocate(r.packSize())
				.order(ByteOrder.BIG_ENDIAN);
		r.pack(buf);

		r.add(data);
		r.add(data2);
		assertEquals(2, r.size());
		buf = ByteBuffer.allocate(r.packSize())
				.order(ByteOrder.BIG_ENDIAN);
		r.pack(buf);
		Record r2 = new Record(ByteBuf.wrap(buf, 0));
		assertEquals(r.packSize(), r2.packSize());
		assertEquals(r2.bufSize(), r2.packSize());
		assertEquals(data, r2.getString(0));

		ByteBuffer buf2 = ByteBuffer.allocate(r2.packSize())
				.order(ByteOrder.BIG_ENDIAN);
		r2.pack(buf2);
		assertEquals(buf.position(0), buf2.position(0));
	}

	@Test
	public void compareTo() {
		Record r = new Record(100);
		Record r2 = new Record(1000);
		assertEquals(r, r);
		assertEquals(r, r2);
		assertEquals(r2, r);
		r.add(data);
		assertFalse(r.equals(r2));
		assertFalse(r2.equals(r));
		assertEquals(+1, Integer.signum(r.compareTo(r2)));
		assertEquals(-1, Integer.signum(r2.compareTo(r)));
		r2.add(data);
		assertEquals(r, r2);
		assertEquals(r2, r);
		r.add(data);
		r.add(data2);
		r2.add(data2);
		r2.add(data);
		assertEquals(-1, Integer.signum(r.compareTo(r2)));
		assertEquals(+1, Integer.signum(r2.compareTo(r)));
	}

	@Test
	public void insert() {
		Record r = make(data, data2);
		assertEquals(data, r.getString(0));
		assertEquals(data2, r.getString(1));
		String s = String.valueOf("hello");
		assertTrue(r.insert(1, s));
		assertEquals(data, r.getString(0));
		assertEquals(s, r.get(1));
		assertEquals(data2, r.getString(2));

		r = new Record(40);
		r.insert(0, s); // insert at beginning
		assertEquals(s, r.get(0));
		r.insert(1, s); // insert at end (same as add)
		assertEquals(s, r.get(0));
		assertFalse(r.insert(1,
				String
				.valueOf(
				"hellooooooooooooooooooooooooooooooooooooooo")));
	}

	@Test
	public void remove() {
		Record r = make(data2, data2, data, data);
		r.remove(2); // middle
		assertEquals(make(data2, data2, data), r);
		r.remove(0); // first
		assertEquals(make(data2, data), r);
		r.remove(1); // last
		assertEquals(make(data2), r);
		r.remove(0);
		assertTrue(r.isEmpty());
	}

	@Test
	public void remove_range() {
		Record r = make(data, data2, data, data2);
		r.remove(1, 3);
		assertEquals(make(data, data2), r);
	}

	@Test
	public void dup() {
		Record r = make(data, data2);
		assertEquals(r, r.dup());
	}

	@Test
	public void unpackLong() {
		Record r = new Record(40);
		r.add(0);
		r.add(1234);
		assertEquals(0, r.getInt(0));
		assertEquals(1234, r.getInt(1));
	}

	static Record make(String... args) {
		Record r = new Record();
		for (String s : args)
			r.add(s);
		return r;
	}

	@Test
	public void bytecmp() {
		byte x = 3;
		byte y = (byte) 130;
		assertEquals(130, (y & 0xff));
		assertTrue(x < (y & 0xff));
	}

	@Test
	public void order() {
		Object values[] = { 0, 70, 140, 9999, 10001 };
		Record prev = null;
		for (Object x : values) {
			Record rec = new Record();
			rec.add(x);
			if (prev != null)
				assertTrue(rec + " should be > " + prev,
						rec.compareTo(prev) > 0);
			prev = rec;
		}
	}

	@Test
	public void hasPrefix() {
		Record rec = new Record(100);
		Record pre = new Record(100);
		assertTrue(rec.hasPrefix(pre));
		pre.add(6);
		assertFalse(rec.hasPrefix(pre));
		rec.add(6);
		assertTrue(rec.hasPrefix(pre));
		rec.add(99);
		assertTrue(rec.hasPrefix(pre));
	}

	@Test
	public void project() {
		ImmutableList<Integer> fields;
		fields = ImmutableList.of();
		assertEquals(make(), make().project(fields, 0));
		assertEquals(make(), make("a", "b").project(fields, 0));
		fields = ImmutableList.of(1, 3);
		assertEquals(make("b", "d"),
				make("a", "b", "c", "d", "e").project(fields, 0));
		assertEquals(make("b").addMin(), make("a", "b").project(fields, 0));
	}

	@Test
	public void misc() {
		assertTrue(MINREC.available() < 1);
		Record rec = new Record();
		rec.addMax();
	}

}
