/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Negative;

/**
 * Base validator to be used in implementations for {@link Negative} constraint.
 *
 * @author Marko Bekhta
 */
public abstract class AbstractNegativeValidator<T> implements ConstraintValidator<Negative, T> {

	private boolean strict;

	@Override
	public void initialize(Negative positive) {
		this.strict = positive.strict();
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}
		int comparisonResult = compare( value );
		return strict ? comparisonResult < 0 : comparisonResult <= 0;
	}

	protected abstract int compare(T element);

}
