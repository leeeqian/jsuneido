package suneido;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Miscellaneous functions.
 *
 * @author Andrew McKinlay
 * <p><small>Copyright 2008 Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.</small></p>
 */
public class Util {

	public static String listToCommas(List<String> list) {
		if (list == null || list.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		for (String s : list)
			sb.append(s).append(",");
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	public static List<String> commasToList(String s) {
		if (s.equals(""))
			return Collections.emptyList();
		return Arrays.asList(s.split(","));
	}

	public static int find(List<String> list, String value) {
		for (int i = 0; i < list.size(); ++i)
			if (list.get(i).equals(value))
				return i;
		return -1;
	}

	public static String bufferToString(ByteBuffer buf) {
		byte[] bytes = new byte[buf.remaining()];
		buf.get(bytes);
		try {
			return new String(bytes, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new SuException("can't unpack string", e);
		}
	}

	/**
	 * @return A new list containing all the values from x and y. x is copied as
	 *         is, so if it has duplicates they are retained. Duplicates from y
	 *         are not retained.
	 */
	public static List<String> set_union(List<String> x, List<String> y) {
		if (x.size() < y.size()) {
			List<String> tmp = x;
			x = y;
			y = tmp;
		}
		List<String> result = new ArrayList<String>(x);
		for (String s : y)
			if (!result.contains(s))
				result.add(s);
		return result;
	}
}
