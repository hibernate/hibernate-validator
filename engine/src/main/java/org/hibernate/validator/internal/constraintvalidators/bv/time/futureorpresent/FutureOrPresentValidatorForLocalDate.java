/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Check that the {@code java.time.LocalDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForLocalDate extends AbstractFutureOrPresentJavaTimeValidator<LocalDate> {

	@Override
	protected LocalDate getReferenceValue(Clock reference) {
		return LocalDate.now( reference );
	}

}
