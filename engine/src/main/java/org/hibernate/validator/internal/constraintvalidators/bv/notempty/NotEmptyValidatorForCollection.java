/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.notempty;

import java.util.Collection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;

/**
 * Check that the collection is not null and not empty.
 *
 * @author Guillaume Smet
 */
// as per the JLS, Collection<?> is a subtype of Collection, so we need to explicitly reference
// Collection here to support having properties defined as Collection (see HV-1551)
@SuppressWarnings("rawtypes")
public class NotEmptyValidatorForCollection implements ConstraintValidator<NotEmpty, Collection> {

	/**
	 * Checks the collection is not {@code null} and not empty.
	 *
	 * @param collection the collection to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the collection is not {@code null} and the collection is not empty
	 */
	@Override
	public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
		if ( collection == null ) {
			return false;
		}
		return collection.size() > 0;
	}
}
