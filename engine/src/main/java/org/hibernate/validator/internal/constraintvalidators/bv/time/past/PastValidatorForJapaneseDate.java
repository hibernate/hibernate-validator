/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.chrono.JapaneseDate;

/**
 * Check that the {@code java.time.chrono.JapaneseDate} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastValidatorForJapaneseDate extends AbstractPastJavaTimeValidator<JapaneseDate> {

	@Override
	protected JapaneseDate getReferenceValue(Clock reference) {
		return JapaneseDate.now( reference );
	}

}
