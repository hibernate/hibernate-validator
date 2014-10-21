/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util;

import java.lang.annotation.Annotation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Emmanuel Bernard
 */
public abstract class BoundariesConstraintValidator<T extends Annotation> implements ConstraintValidator<T, Integer> {
	private int low;
	private int high;

	protected void initialize(int low, int high) {
		this.low = low;
		this.high = high;
	}

	public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
		return value >= low && value <= high;
	}
}
