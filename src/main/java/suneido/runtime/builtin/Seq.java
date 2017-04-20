/* Copyright 2009 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.runtime.builtin;

import static java.lang.Boolean.FALSE;

import java.util.Iterator;
import java.util.NoSuchElementException;

import suneido.runtime.*;

public final class Seq extends BuiltinClass {
	private static final FunctionSpec initFS =
			ArgsArraySpec.from("from = false, to = false, by = 1");

	public Seq() {
		super("Seq", initFS);
	}

	@Override
	public Object newInstance(Object... args) {
		args = Args.massage(initFS, args);
		return new Sequence(new SuSeq(args[0], args[1], args[2]));
	}

	private static class SuSeq implements Iterable<Object>, Sequence.Infinitable {
		private final Object from;
		private final Object to;
		private final Object by;
		private final boolean infinite;

		SuSeq(Object from, Object to, Object by) {
			if (infinite = (from == FALSE)) {
				from = 0;
				to = Integer.MAX_VALUE;
			} else if (to == FALSE) {
				to = from;
				from = 0;
			}
			this.from = from;
			this.to = to;
			this.by = by;
		}

		@Override
		public String toString() {
			return infinite
					? "Seq()"
					: "Seq(" + from + ", " + to + ", " + by + ")";
		}

		@Override
		public Iterator<Object> iterator() {
			return new Iter();
		}

		class Iter implements Iterator<Object> {
			Object i = from;

			@Override
			public boolean hasNext() {
				return Ops.cmp(i, to) < 0;
			}

			@Override
			public Object next() {
				if (! hasNext())
					throw new NoSuchElementException();
				Object x = i;
				i = Ops.add(i, by);
				return x;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		}

		@Override
		public boolean infinite() {
			return infinite;
		}

	}

}
