/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.classlevelconstraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.ap.testmodel.classlevelconstraints.ClassLevelValidation.Customer;

public class ValidCustomerValidator implements ConstraintValidator<ValidCustomer, Customer> {
	@Override
	public void initialize(ValidCustomer constraintAnnotation) {
	}

	@Override
	public boolean isValid(Customer customer, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
