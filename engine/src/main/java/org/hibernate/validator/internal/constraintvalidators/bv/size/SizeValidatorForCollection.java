/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.size;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that a collection's size is between min and max.
 *
 * @author Hardy Ferentschik
 */
@SuppressWarnings("rawtypes")
// as per the JLS, Collection<?> is a subtype of Collection, so we need to explicitly reference
// Collection here to support having properties defined as Collection (see HV-1551)
public class SizeValidatorForCollection implements ConstraintValidator<Size, Collection> {

	private  static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int min;
	private int max;

	@Override
	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	/**
	 * Checks the number of entries in a collection.
	 *
	 * @param collection the collection to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 *
	 * @return {@code true} if the collection is {@code null} or the number of entries in
	 *         {@code collection} is between the specified {@code min} and {@code max} values (inclusive),
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
		if ( collection == null ) {
			return true;
		}
		int length = collection.size();
		return length >= min && length <= max;
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw LOG.getMinCannotBeNegativeException();
		}
		if ( max < 0 ) {
			throw LOG.getMaxCannotBeNegativeException();
		}
		if ( max < min ) {
			throw LOG.getLengthCannotBeNegativeException();
		}
	}
}
