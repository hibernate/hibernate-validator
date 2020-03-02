/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.size;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that a map's size is between min and max.
 *
 * @author Hardy Ferentschik
 */
// as per the JLS, Map<?, ?> is a subtype of Map, so we need to explicitly reference
// Map here to support having properties defined as Map (see HV-1551)
@SuppressWarnings("rawtypes")
public class SizeValidatorForMap implements ConstraintValidator<Size, Map> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int min;
	private int max;

	@Override
	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	/**
	 * Checks the number of entries in a map.
	 *
	 * @param map The map to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns {@code true} if the map is {@code null} or the number of entries in {@code map}
	 *         is between the specified {@code min} and {@code max} values (inclusive),
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isValid(Map map, ConstraintValidatorContext constraintValidatorContext) {
		if ( map == null ) {
			return true;
		}
		int size = map.size();
		return size >= min && size <= max;
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw LOG.getMaxCannotBeNegativeException();
		}
		if ( max < 0 ) {
			throw LOG.getMaxCannotBeNegativeException();
		}
		if ( max < min ) {
			throw LOG.getLengthCannotBeNegativeException();
		}
	}
}
