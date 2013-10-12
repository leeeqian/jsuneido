/* Copyright 2009 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.language;

import static suneido.SuContainer.IterResult.ENTRY;
import static suneido.SuContainer.IterWhich.ALL;
import static suneido.language.Args.Special.EACH;
import static suneido.language.Args.Special.EACH1;
import static suneido.language.Args.Special.NAMED;

import java.util.AbstractMap;
import java.util.Iterator;

import suneido.util.NullIterator;

import com.google.common.collect.Iterators;

public class ArgsIterator implements Iterator<Object>, Iterable<Object> {
	private final Object[] args;
	private boolean named = true;
	private int argi = 0;
	Iterator<Object> each = new NullIterator<Object>();

	public ArgsIterator(Object[] args) {
		this.args = args;
	}

	@Override
	public boolean hasNext() {
		if (each.hasNext())
			return true;
		if (argi >= args.length)
			return false;
		Object x = args[argi];
		if (x != EACH && x != EACH1)
			return true;
		return Ops.toContainer(args[argi + 1]).size() > (x == EACH1 ? 1 : 0);
	}

	@Override
	public Object next() {
		if (each.hasNext())
			return each.next();
		Object x = args[argi++];
		if (x == EACH || x == EACH1) {
			each = Ops.toContainer(args[argi++]).iterator(ALL, ENTRY);
			if (x == EACH1 && each.hasNext())
				each.next();
			return next();
		} else if (x == NAMED && named) {
			x = args[argi++];
			return new AbstractMap.SimpleEntry<Object, Object>(x, args[argi++]);
		}
		return x;
	}

	public Object[] rest() {
		named = false;
		return Iterators.toArray(this, Object.class);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * to allow: for (x : ArgsIterator(args))
	 */
	@Override
	public Iterator<Object> iterator() {
		return this;
	}

}
