/* Copyright 2013 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.language.jsdi.type;

import java.util.Arrays;

import suneido.SuException;
import suneido.SuValue;
import suneido.language.Ops;
import suneido.language.jsdi.*;

/**
 * TODO: docs
 * 
 * @author Victor Schappert
 * @since 20130625
 */
@DllInterface
public final class Callback extends ComplexType {

	//
	// DATA
	//

	private final ThunkManager thunkManager;
	private MarshallPlan marshallPlan;

	//
	// CONSTRUCTORS
	//

	Callback(String valueName, TypeList parameters, ThunkManager thunkManager) {
		super(TypeId.CALLBACK, valueName, parameters);
		if (null == thunkManager) {
			throw new IllegalArgumentException("thunkManager cannot be null");
		}
		this.thunkManager = thunkManager;
		this.marshallPlan = null;
	}

	//
	// INTERNALS
	//

	private static int toInt(Object result) {
		if (null == result) {
			// For consistency with CSuneido, a 'null' return value (failure to
			// return any value, as in 'function() { }' or
			// 'function() { return }') should send back 0 to the invoking DLL.
			return 0;
		}
		return Ops.toInt(result);
	}

	//
	// ACCESSORS
	//

	// TODO: docs since 20130806
	public MarshallPlan getMarshallPlan() {
		// TODO: resolve thread safety and update issues --
		//       this will cause problems if marshall plan on an already bound
		//       thunk can change
		try {
			if (typeList.resolve(0) || null == marshallPlan) {
				marshallPlan = typeList.makeParamsMarshallPlan(true, false);
			}
		} catch (ProxyResolveException e) {
			e.setMemberType("parameter");
			e.setParentName(valueName());
			throw new JSDIException(e);
		}
		return marshallPlan;
	}

	/**
	 * Invoked from native side.
	 * 
	 * @param boundValue
	 *            Bound value to invoke
	 * @param argsIn
	 *            Argument array to unmarshall
	 * @return The return value of {@code callable}, which must be coerceable
	 *         to an {@code int}
	 * @since 20130806
	 * @see #invokeVariableIndirect(SuValue, byte[], Object[])
	 */
	public int invoke(SuValue boundValue, byte[] argsIn) {
		Marshaller marshaller = marshallPlan.makeUnMarshaller(argsIn);
		Object[] argsOut = typeList.marshallOutParams(marshaller);
		Object result = boundValue.call(argsOut);
		return toInt(result);
	}

	/**
	 * Invoked from native side.
	 * 
	 * @param boundValue
	 *            Bound value to invoke
	 * @param argsIn
	 *            Argument array to unmarshall
	 * @param viArray
	 *            Variable indirect component of arguments
	 * @return The return value of {@code callable}, which must be coerceable
	 *         to an {@code int}
	 * @since 20130806
	 * @see #invoke(SuValue, byte[])
	 */
	public int invokeVariableIndirect(SuValue boundValue, byte[] argsIn,
			Object[] viArray) {
		int[] viInstArray = new int[viArray.length];
		Arrays.fill(viInstArray,
				VariableIndirectInstruction.RETURN_JAVA_STRING.ordinal());
		Marshaller marshaller = marshallPlan.makeUnMarshaller(argsIn, viArray,
				viInstArray);
		Object[] argsOut = typeList.marshallOutParams(marshaller);
		Object result = boundValue.call(argsOut);
		return toInt(result);
	}

	//
	// ANCESTOR CLASS: Type
	//

	@Override
	public String getDisplayName() {
		return "callback" + typeList.toParamsTypeString();
	}

	@Override
	public int getSizeDirectIntrinsic() {
		return PrimitiveSize.POINTER;
	}

	@Override
	public int getSizeDirectWholeWords() {
		return PrimitiveSize.pointerWholeWordBytes();
	}

	@Override
	@_64BitIssue
	public void marshallIn(Marshaller marshaller, Object value) {
		if (null == value) {
			marshaller.putLong(0);
		} else if (value instanceof SuValue) {
			int thunkFuncAddr = thunkManager.lookupOrCreateBoundThunk(
					(SuValue) value, this);
			marshaller.putLong(thunkFuncAddr);
		} else try {
			marshaller.putLong(NumberConversions.toPointer32(value));
		} catch (SuException e) {
			throw new JSDIException("can't marshall " + value + " into "
					+ toString());
		}
	}

	@Override
	public Object marshallOut(Marshaller marshaller, Object oldValue) {
		return oldValue; // Nothing to be done here
	}

	//
	// ANCESTOR CLASS: Object
	//

	@Override
	public String toString() {
		return getDisplayName(); // Can be result of Suneido 'Display' built-in.
	}
}
