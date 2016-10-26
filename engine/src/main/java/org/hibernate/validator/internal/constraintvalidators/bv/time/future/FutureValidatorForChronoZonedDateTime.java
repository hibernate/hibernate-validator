/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;

/**
 * Check that the {@code java.time.chrono.ChronoZonedDateTime} passed is in the future.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class FutureValidatorForChronoZonedDateTime extends AbstractFutureJavaTimeValidator<ChronoZonedDateTime<?>> {

	@Override
	protected ChronoZonedDateTime<?> getReferenceValue(Clock reference) {
		return ZonedDateTime.now( reference );
	}

}
