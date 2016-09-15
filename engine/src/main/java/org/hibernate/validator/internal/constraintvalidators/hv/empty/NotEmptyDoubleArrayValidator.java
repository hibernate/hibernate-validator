/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.empty;

/**
 * Check that double array length is greater than zero.
 *
 */
public class NotEmptyDoubleArrayValidator extends  NotEmptyBaseValidator<double[]> {

	@Override
	protected boolean isNotEmpty( double[] element ) {
		return element.length > 0;
	}
}
