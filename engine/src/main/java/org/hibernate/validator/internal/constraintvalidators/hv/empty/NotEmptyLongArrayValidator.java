/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.empty;

/**
 * Check that long array length is greater than zero.
 *
 */
public class NotEmptyLongArrayValidator extends  NotEmptyBaseValidator<long[]> {

	@Override
	protected boolean isNotEmpty( long[] element ) {
		return element.length > 0;
	}
}
