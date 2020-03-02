/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.size;

import java.lang.invoke.MethodHandles;

import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the length of an array is between <i>min</i> and <i>max</i>
 *
 * @author Hardy Ferentschik
 */
public abstract class SizeValidatorForArraysOfPrimitives {

	private  static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	protected int min;
	protected int max;

	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
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
