/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.classlevelconstraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
