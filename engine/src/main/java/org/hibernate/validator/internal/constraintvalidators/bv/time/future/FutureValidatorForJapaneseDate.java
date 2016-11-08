/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.chrono.JapaneseDate;

/**
 * Check that the {@code java.time.chrono.JapaneseDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForJapaneseDate extends AbstractFutureJavaTimeValidator<JapaneseDate> {

	@Override
	protected JapaneseDate getReferenceValue(Clock reference) {
		return JapaneseDate.now( reference );
	}

}
