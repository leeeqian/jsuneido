/* Copyright 2012 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.runtime;

import java.nio.ByteBuffer;

import suneido.SuValue;
import suneido.runtime.builtin.StringMethods;

/**
 * Abstract base class for string-like values {@link Concats} and {@link Except}
 * Used for type check in {@link Ops} is_
 * and for common implementations of {@link SuValue} and Comparable methods
 */
public abstract class String2 extends SuValue
		implements Comparable<String2>, CharSequence {

	@Override
	public Object call(Object... args) {
		return Ops.callString(toString(), args);
	}

	@Override
	public Object get(Object member) {
		return Ops.get(toString(), member);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		return other != null && other.equals(toString());
	}

	@Override
	public int compareTo(String2 other) {
		return toString().compareTo(other.toString());
	}

	@Override
	public int packSize(int nest) {
		return Pack.packSize(toString());
	}

	@Override
	public void pack(ByteBuffer buf) {
		Pack.packString(toString(), buf);
	}

	@Override
	public SuValue lookup(String method) {
		return StringMethods.singleton.lookup(method);
	}

	@Override
	public String typeName() {
		return "String";
	}

}
