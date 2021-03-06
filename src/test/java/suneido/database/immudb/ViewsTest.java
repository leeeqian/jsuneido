/* Copyright 2011 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.database.immudb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class ViewsTest {
	private static final String NAME = "myview";
	private static final Record KEY = new RecordBuilder().add(NAME).build();
	private static final String DEFINITION = "tables join columns";
	private static final DataRecord VIEW_REC =
			new RecordBuilder().add(NAME).add(DEFINITION).build();

	//TODO

//	@Test
//	public void addView() {
//		SchemaTransaction t = mock(SchemaTransaction.class);
//		Views.addView(t, NAME, DEFINITION);
//		verify(t).addRecord(Bootstrap.TN.VIEWS, VIEW_REC);
//	}

	@Test
	public void getView_missing() {
		ReadTransaction t = mock(ReadTransaction.class);
		assertNull(Views.getView(t, NAME));
	}

	@Test
	public void getView() {
		ReadTransaction t = mock(ReadTransaction.class);
		when(t.lookup(Bootstrap.TN.VIEWS, Views.INDEX_COLS, KEY)).thenReturn(VIEW_REC);
		assertThat(Views.getView(t, NAME), equalTo(DEFINITION));
	}

//	@Test
//	public void dropView() {
//		SchemaTransaction t = mock(SchemaTransaction.class);
//		when(t.lookup(Bootstrap.TN.VIEWS, Views.INDEX_COLS, KEY)).thenReturn(VIEW_REC);
//		Views.dropView(t, NAME);
//		verify(t).removeRecord(Bootstrap.TN.VIEWS, VIEW_REC);
//	}

}
