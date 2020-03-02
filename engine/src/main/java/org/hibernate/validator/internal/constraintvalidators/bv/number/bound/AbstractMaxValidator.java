/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Max;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 * @author Marko Bekhta
 */
public abstract class AbstractMaxValidator<T> implements ConstraintValidator<Max, T> {

	protected long maxValue;

	@Override
	public void initialize(Max maxValue) {
		this.maxValue = maxValue.value();
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext constraintValidatorContext) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return compare( value ) <= 0;
	}

	protected abstract int compare(T number);
}
