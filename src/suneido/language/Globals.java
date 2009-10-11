package suneido.language;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import suneido.*;
import suneido.language.builtin.*;

/**
 * Stores global names and values.
 * Uses the class itself as a singleton by making everything static.
 * @author Andrew McKinlay
 * <p><small>Copyright 2008 Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.</small></p>
 */
public class Globals {
	private static final Map<String, Object> globals =
			new ConcurrentHashMap<String, Object>(1000);
	private static final Map<String, Object> builtins =
			new ConcurrentHashMap<String, Object>(100);
	private static Integer overload = 0;
	static {
		builtins.put("True", Boolean.TRUE);
		builtins.put("False", Boolean.FALSE);
		builtins.put("Suneido", new SuContainer());
		builtins.put("Date", new DateClass());
		builtins.put("Object", new ObjectClass());
		builtins.put("Sleep", new Sleep());
		builtins.put("DeleteFile", new DeleteFile());
		builtins.put("FileExists?", new FileExistsQ());
		builtins.put("File", new FileClass());
	}

	private Globals() { // no instances
		throw SuException.unreachable();
	}

	public static void builtin(String name, Object value) {
		builtins.put(name, value);
	}

	public static int size() {
		return globals.size();
	}

	public static Object get(String name) {
		Object x = tryget(name);
		if (x == null)
			throw new SuException("can't find " + name);
		return x;
	}

	/**
	 * does NOT prevent two threads concurrently getting same name but this
	 * shouldn't matter since it idempotent i.e. result should be the same no
	 * matter which thread "wins"
	 */
	public static Object tryget(String name) {
		Object x = globals.get(name);
		if (x != null)
			return x;
		x = builtins.get(name);
		if (x != null) {
			globals.put(name, x);
			return x;
		}
		x = Libraries.load(name);
		if (x == null) {
			x = loadClass(name);
			if (x != null)
				builtins.put(name, x);
		}
		if (x != null)
			globals.put(name, x);
		// PERF could save a special value to avoid future attempts to load
		return x;
	}

	private static Object loadClass(String name) {
		name = CompileGenerator.javify(name);
		Class<?> c = null;
		try {
			c = Class.forName("suneido.language.builtin." + name);
		} catch (ClassNotFoundException e) {
			return null;
		}
		SuValue sc = null;
		try {
			sc = (SuValue) c.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		//System.out.println("<loaded: " + name + ">");
		return sc;
	}

	/** used by tests */
	public static void put(String name, Object x) {
		globals.put(name, x);
	}

	public static void unload(String name) {
		globals.remove(name);
	}

	/** for Libraries.use */
	public static void clear() {
		globals.clear();
	}

	// TODO make this thread safe (not sufficient to synchronize)
	public static String overload(String base) {
		String name = base.substring(1);
		Object x = globals.get(name);
		if (x == null)
			throw new SuException("can't find " + base);
		String nameForPreviousValue = overload.toString() + base;
		globals.put(nameForPreviousValue, get(name));
		return nameForPreviousValue;
	}

}
