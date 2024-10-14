/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
