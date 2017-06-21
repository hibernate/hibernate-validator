/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.Year;

/**
 * Check that the {@code java.time.Year} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForYear extends AbstractPastOrPresentJavaTimeValidator<Year> {

	@Override
	protected Year getReferenceValue(Clock reference) {
		return Year.now( reference );
	}

}
