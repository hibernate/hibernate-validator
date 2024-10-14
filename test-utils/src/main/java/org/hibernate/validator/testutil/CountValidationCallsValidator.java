/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class CountValidationCallsValidator implements ConstraintValidator<CountValidationCalls, Object> {
	private static final ThreadLocal<Integer> NUMBER_OF_VALIDATION_CALLS = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};

	public static void init() {
		NUMBER_OF_VALIDATION_CALLS.set( 0 );
	}

	public static int getNumberOfValidationCall() {
		return NUMBER_OF_VALIDATION_CALLS.get();
	}

	@Override
	public void initialize(CountValidationCalls constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		NUMBER_OF_VALIDATION_CALLS.set( NUMBER_OF_VALIDATION_CALLS.get() + 1 );
		return true;
	}
}
