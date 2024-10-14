/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;

import jakarta.validation.constraints.Size;

public class MethodLevelValidationUsingBuiltInConstraints {
	@Size(min = 10)
	public String getString() {
		return null;
	}

	/**
	 * Not allowed. Method is no getter.
	 */
	@Size(min = 10)
	public void setString() {
	}

	/**
	 * Not allowed. Return type doesn't match.
	 */
	@Size(min = 10)
	public Date getDate() {
		return null;
	}

	/**
	 * Not allowed. No return type.
	 */
	@Size(min = 10)
	public void getAnotherString() {
	}

	/**
	 * Not allowed. Static method.
	 */
	@Size(min = 10)
	public static String getStringStatically() {
		return null;
	}

	/**
	 * No getter, but allowed with -AmethodConstraintsSupported.
	 */
	@Size(min = 10)
	public String doSomething() {
		return null;
	}

	/**
	 * Also with -AmethodConstraintsSupported not allowed, as return type doesn't match.
	 */
	@Size(min = 10)
	public Date doSomethingReturningDate() {
		return null;
	}

	/**
	 * Also with -AmethodConstraintsSupported not allowed. No return type.
	 */
	@Size(min = 10)
	public void voidDoSomething() {
	}

	/**
	 * Also with -AmethodConstraintsSupported not allowed. Static method.
	 */
	@Size(min = 10)
	public static String staticDoSomething() {
		return null;
	}

}
