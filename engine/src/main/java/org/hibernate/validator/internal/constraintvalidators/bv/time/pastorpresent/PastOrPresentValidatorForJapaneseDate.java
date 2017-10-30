/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.chrono.JapaneseDate;

import javax.validation.constraints.PastOrPresent;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractTemporalValidator;

/**
 * Check that the {@code java.time.chrono.JapaneseDate} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForJapaneseDate extends AbstractTemporalValidator<PastOrPresent, JapaneseDate> {

	@Override
	protected JapaneseDate getReferenceValue(Clock reference) {
		return JapaneseDate.now( reference );
	}

}
