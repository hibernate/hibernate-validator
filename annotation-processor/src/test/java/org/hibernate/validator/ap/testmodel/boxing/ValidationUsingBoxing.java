/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
