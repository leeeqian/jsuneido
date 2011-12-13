/* Copyright 2008 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.language;

import suneido.SuValue;

public abstract class SuCallable extends SuValue {
	protected SuClass myClass;
	protected FunctionSpec params;
	protected boolean isBlock = false;

	@Override
	public SuValue lookup(String method) {
		if (method == "Params")
			return Params;
		return super.lookup(method);
	}

	private static SuValue Params = new SuMethod0() {
		@Override
		public Object eval0(Object self) {
			return ((SuCallable) self).params.params();
		}
	};

	@Override
	public boolean isCallable() {
		return true;
	}

	public Object superInvoke(Object self, String member, Object... args) {
		return myClass.superInvoke(self, member, args);
	}

	@Override
	public String toString() {
		return super.typeName().replace(AstCompile.METHOD_SEPARATOR, '.');
	}

	protected Object defaultFor(int i) {
		assert params != null : "" + this + " has no params";
		return params.defaultFor(i);
	}

	public static boolean isBlock(Object x) {
		return x instanceof SuCallable && ((SuCallable) x).isBlock;
	}

}
