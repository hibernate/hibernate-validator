/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Check that the {@code java.time.OffsetDateTime} passed is in the past.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForOffsetDateTime extends AbstractPastOrPresentJavaTimeValidator<OffsetDateTime> {

	@Override
	protected OffsetDateTime getReferenceValue(Clock reference) {
		return OffsetDateTime.now( reference );
	}

}
