/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.composition.lazyevaluation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class InvocationCountingValidator extends InvocationCounter
		implements ConstraintValidator<InvocationCounting, Object> {

	@Override
	public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
		incrementCount( o );
		return false;
	}
}

