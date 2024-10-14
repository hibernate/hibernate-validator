/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
