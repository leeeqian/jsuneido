/* Copyright 2011 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.database.immudb;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import suneido.database.immudb.Bootstrap.TN;
import suneido.util.Immutable;

/**
 * Table schema information.
 * @see Columns
 * @see Indexes
 */
@Immutable
public class Table {
	static final int TBLNUM = 0, TABLE = 1;
	final int num;
	final String name;
	final Columns columns;
	final Indexes indexes;
	final ImmutableList<String> fields;

	Table(int num, String name, Columns columns, Indexes indexes) {
		checkNotNull(columns);
		checkNotNull(indexes);
		checkIndexTblnum(num, indexes);
		this.num = num;
		this.name = name;
		this.columns = columns;
		this.indexes = indexes;
		fields = buildFields();
	}

	public static boolean isSpecialField(String column) {
		return column.endsWith("_lower!");
	}

	private static void checkIndexTblnum(int num, Indexes indexes) {
		for (Index idx : indexes)
			assert idx.tblnum == num;
	}

	Table(Record record, Columns columns, Indexes indexes) {
		this(record.getInt(TBLNUM), record.getString(TABLE), columns, indexes);
	}

	public String name() {
		return name;
	}

	public int num() {
		return num;
	}

	DataRecord toRecord() {
		return toRecord(num, name);
	}

	static DataRecord toRecord(int num, String name) {
		DataRecord r = new RecordBuilder().add(num).add(name).build();
		r.tblnum(TN.TABLES);
		return r;
	}

	boolean hasColumn(String name) {
		return columns.hasColumn(name);
	}

	Column getColumn(String name) {
		return columns.find(name);
	}

	int maxColumnNum() {
		return columns.maxNum();
	}

	boolean hasIndexes() {
		return !indexes.isEmpty();
	}

	Index firstIndex() {
		return indexes.first();
	}

	Index getIndex(String colNames) {
		return getIndex(namesToNums(colNames));
	}

	Index getIndex(int[] colNums) {
		return indexes.getIndex(colNums);
	}

	public boolean singleton() {
		return indexes.first().colNums.length == 0;
	}

	public List<String> getColumns() {
		return columns.names();
	}

	public List<List<String>> indexesColumns() {
		return indexes.columns(columns);
	}

	public List<List<String>> keysColumns() {
		return indexes.keysColumns(columns);
	}

	List<Column> columnsList() {
		return columns.columns;
	}

	List<Index> indexesList() {
		return indexes.indexes;
	}

	int[] namesToNums(String names) {
		return columns.numsArray(names);
	}

	String numsToNames(int[] nums) {
		return columns.names(nums);
	}

	/**
	 * @return The physical fields. 1:1 match with records.
	 */
	public ImmutableList<String> getFields() {
		return fields;
	}

	private ImmutableList<String> buildFields() {
		ImmutableList.Builder<String> list = ImmutableList.builder();
		int i = 0;
		for (Column cs : columns) {
			if (cs.field < 0)
				continue; // skip rules
			for (; i < cs.field; ++i)
				list.add("-");
			list.add(cs.name);
			++i;
		}
		return list.build();
	}

	String schema() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(columns.schemaColumns()).append(")");
		for (Index index : indexes)
			index.schema(sb, columns);
		return sb.toString();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("num", num)
				.add("columns", columns)
				.add("indexes", indexes)
				.toString();
	}

}
