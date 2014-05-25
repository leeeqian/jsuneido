/* Copyright 2012 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.util;

import java.nio.ByteBuffer;

public class ByteBuffers {

	public static String bufferToString(ByteBuffer buf) {
		return ByteBuffers.getStringFromBuffer(buf, buf.position());
	}

	/** does NOT change buffer position */
	public static String getStringFromBuffer(ByteBuffer buf, int i) {
		StringBuilder sb = new StringBuilder(buf.remaining());
		for (; i < buf.limit(); ++i)
			sb.append((char) (buf.get(i) & 0xff));
		return sb.toString();
	}

	public static ByteBuffer stringToBuffer(String s) {
		ByteBuffer buf = ByteBuffer.allocate(s.length());
		ByteBuffers.putStringToByteBuffer(s, buf, 0);
		return buf;
	}

	/** DOES change buffer position */
	public static void putStringToByteBuffer(String s, ByteBuffer buf) {
		for (int i = 0; i < s.length(); ++i)
			buf.put((byte) s.codePointAt(i));
	}

	/** does NOT change buffer position */
	public static void putStringToByteBuffer(String s, ByteBuffer buf, int pos) {
		for (int i = 0; i < s.length(); ++i)
			buf.put(pos++, (byte) s.codePointAt(i));
	}

	public static String bufferToHex(ByteBuffer buf) {
		StringBuilder sb = new StringBuilder();
		for (int i = buf.position(); i < buf.limit(); ++i)
			sb.append(" ").append(String.format("%02x", buf.get(i)));
		return sb.substring(1);
	}

	public static String bufferToHex(byte[] buf, int i, int n) {
		StringBuilder sb = new StringBuilder();
		for (; i < n; ++i)
			sb.append(" ").append(ByteBuffers.format(buf[i]));
		return sb.substring(1);
	}

	private static String format(byte b) {
		String s = String.format("%02x", b);
		if (32 <= b && b <= 126)
			s += " '" + (char) b + "' ";
		return s;
	}

	public static int bufferUcompare(ByteBuffer b1, ByteBuffer b2) {
		int n = Math.min(b1.remaining(), b2.remaining());
		int b1pos = b1.position();
		int b2pos = b2.position();
		for (int i = 0; i < n; ++i) {
			int cmp = (b1.get(b1pos + i) & 0xff) - (b2.get(b2pos + i) & 0xff);
			if (cmp != 0)
				return cmp;
		}
		return b1.remaining() - b2.remaining();
	}

	public static ByteBuffer copyByteBuffer(ByteBuffer buf, int size) {
		byte[] data = new byte[size];
		// duplicate buffer if we need to set position
		// because modification is not thread safe
		if (buf.position() != 0) {
			buf = buf.duplicate();
			buf.position(0);
		}
		buf.get(data);
		return ByteBuffer.wrap(data);
	}

	public static final ByteBuffer EMPTY_BUF = ByteBuffer.allocate(0);

	/**
	 * Return a slice of a buffer, ignoring the buffer position.<p>
	 * The position of the result will be 0, and the limit will be len.<p>
	 * Avoids modifying the original so thread safe.<p>
	 * Always returns a new ByteBuffer (except when len is 0).<p>
	 * @return A slice of a ByteBuffer.
	 */
	public static ByteBuffer slice(ByteBuffer buf, int pos, int len) {
		if (len == 0)
			return EMPTY_BUF;
		assert pos + len <= buf.limit();
		buf = buf.duplicate();
		buf.position(pos);
		buf.limit(pos + len);
		if (pos != 0)
			buf = buf.slice();
		return buf;
	}

}
