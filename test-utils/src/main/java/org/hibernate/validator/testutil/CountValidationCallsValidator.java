/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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

	public void initialize(CountValidationCalls constraintAnnotation) {
	}

	public boolean isValid(Object value, ConstraintValidatorContext context) {
		NUMBER_OF_VALIDATION_CALLS.set( NUMBER_OF_VALIDATION_CALLS.get() + 1 );
		return true;
	}
}
