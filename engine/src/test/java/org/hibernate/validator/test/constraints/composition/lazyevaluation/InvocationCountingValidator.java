/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
