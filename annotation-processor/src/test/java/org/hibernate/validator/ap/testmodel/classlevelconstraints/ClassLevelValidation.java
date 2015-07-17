/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.classlevelconstraints;

public class ClassLevelValidation {
	@ValidCustomer
	public static class Customer {
	}

	/**
	 * Not allowed.
	 */
	@ValidCustomer
	public static class Order {
	}
}
