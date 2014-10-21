/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;
import javax.validation.constraints.Size;

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
