package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.Instant;

/**
 * Check that the {@code java.time.Instant} passed is in the past.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class PastValidatorForInstant extends AbstractPastJavaTimeValidator<Instant> {

	@Override
	protected Instant getReferenceValue(Clock reference) {
		return Instant.now( reference );
	}

}
