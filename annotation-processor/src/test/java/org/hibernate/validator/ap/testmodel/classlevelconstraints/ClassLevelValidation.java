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
