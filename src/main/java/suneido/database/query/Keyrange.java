/* Copyright 2008 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.database.query;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import suneido.database.immudb.Dbpkg;
import suneido.database.immudb.Record;

public class Keyrange {
	Record org;
	Record end;

	public Keyrange() {
		setAll();
	}

	public Keyrange(Record org, Record end) {
		set(org, end);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("org", org)
			.add("end", end)
			.toString();
	}

	public boolean isEmpty() {
		return org.compareTo(end) > 0;
	}

	public Keyrange set(Record org, Record end) {
		this.org = org;
		this.end = end;
		return this;
	}

	public Keyrange setAll() {
		org = Dbpkg.MIN_RECORD;
		end = Dbpkg.MAX_RECORD;
		return this;
	}

	public Keyrange setNone() {
		org = Dbpkg.MAX_RECORD;
		end = Dbpkg.MIN_RECORD;
		return this;
	}

	public boolean equals(Record org, Record end) {
		return Objects.equal(org, this.org) &&
				Objects.equal(end, this.end);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Keyrange))
			return false;
		Keyrange kr = (Keyrange) other;
		return Objects.equal(org, kr.org) &&
				Objects.equal(end, kr.end);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(org, end);
	}

	public boolean contains(Record key) {
		return org.compareTo(key) <= 0 && end.compareTo(key) >= 0;
	}

	public static Keyrange intersect(Keyrange r1, Keyrange r2) {
		return new Keyrange(max(r1.org, r2.org), min(r1.end, r2.end));
	}

	public static Record min(Record r1, Record r2) {
		return r1.compareTo(r2) <= 0 ? r1 : r2;
	}

	public static Record max(Record r1, Record r2) {
		return r1.compareTo(r2) >= 0 ? r1 : r2;
	}

}
