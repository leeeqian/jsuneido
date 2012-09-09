/* Copyright 2010 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.language;

public class SuBlock2 extends SuBlock {

	public SuBlock2(Object block, Object self, Object[] locals) {
		super(block, self, locals);
	}

	@Override
	public Object call2(Object a, Object b) {
		int i = bspec.iparams;
		locals[i] = a;
		locals[++i] = b;
		return block.eval(self, locals);
	}

	@Override
	public Object eval2(Object newSelf, Object a, Object b) {
		int i = bspec.iparams;
		locals[i] = a;
		locals[++i] = b;
		return block.eval(newSelf, locals);
	}

}
