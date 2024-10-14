/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.boxing;

public class ValidationUsingBoxing {

	@ValidLong
	public long primitiveLongField;

	@ValidLong
	public Long longField;

	/**
	 * Not allowed.
	 */
	@ValidLong
	public int intField;

	/**
	 * Not allowed.
	 */
	@ValidLong
	public Integer integerField;

	/**
	 * Not allowed.
	 */
	@ValidLong
	public double doubleField;

	@ValidLong
	public long getPrimitiveLong() {
		return 0;
	}

	@ValidLong
	public Long getLong() {
		return Long.MIN_VALUE;
	}

	/**
	 * Not allowed.
	 */
	@ValidLong
	public int getInt() {
		return 0;
	}

	/**
	 * Not allowed.
	 */
	@ValidLong
	public Integer getInteger() {
		return Integer.MIN_VALUE;
	}

}
