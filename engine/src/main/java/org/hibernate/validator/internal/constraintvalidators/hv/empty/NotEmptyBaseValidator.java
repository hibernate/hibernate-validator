/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.empty;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Check that an element length is greater than zero.
 */
public abstract class NotEmptyBaseValidator<T> implements ConstraintValidator<NotEmpty, T> {

	private boolean canBeNull = false;

	@Override
	public void initialize( NotEmpty annotation ) {
		this.canBeNull = annotation.canBeNull();
	}

	/**
	 * Checks that the element is not empty.
	 *
	 * @param element                    The element to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 * @return By default returns {@code true} if the element is not {@code null} or the size of {@code element} is greater than zero,
	 * if parameter {@code canBeNull} is set to {@code true} it allows {@code null} values and returns {@code true} ,
	 * otherwise {@code false} is returned.
	 */
	@Override
	public boolean isValid( T element, ConstraintValidatorContext constraintValidatorContext ) {
		if ( !canBeNull && element == null ) {
			return false;
		}
		if ( element == null ) {
			return true;
		}

		return isNotEmpty( element );
	}

	protected abstract boolean isNotEmpty( T element );
}
