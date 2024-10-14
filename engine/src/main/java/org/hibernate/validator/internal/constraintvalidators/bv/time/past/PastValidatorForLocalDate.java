/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Check that the {@code java.time.LocalDate} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastValidatorForLocalDate extends AbstractPastJavaTimeValidator<LocalDate> {

	@Override
	protected LocalDate getReferenceValue(Clock reference) {
		return LocalDate.now( reference );
	}

}
