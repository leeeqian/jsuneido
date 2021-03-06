/* Copyright 2008 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static suneido.util.ArraysList.CHUNK_SIZE;

import org.junit.Test;

public class IntArraysListTest {

	@Test
	public void test() {
		IntArraysList list = new IntArraysList();
		assertThat(list.size(), equalTo(0));

		list.add(0);
		assertThat(list.size(), equalTo(1));
		assertThat(list.get(0), equalTo(0));

		for (int i = 1; i < 3 * CHUNK_SIZE; ++i)
			list.add(i * 2);
		for (int i = 1; i < 3 * CHUNK_SIZE; ++i)
			assertThat(list.get(i), equalTo(i * 2));

		list.set(5, 555);
		assertThat(list.get(5), equalTo(555));

		list.set(5 + CHUNK_SIZE, -1);
		assertThat(list.get(5 + CHUNK_SIZE), equalTo(-1));

		list.clear();
		assertThat(list.size(), equalTo(0));
	}

}
