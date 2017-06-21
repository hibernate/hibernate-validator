/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Clock;
import java.time.Instant;

/**
 * Check that the {@code java.time.Instant} passed is in the future.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForInstant extends AbstractFutureOrPresentJavaTimeValidator<Instant> {

	@Override
	protected Instant getReferenceValue(Clock reference) {
		return Instant.now( reference );
	}

}
