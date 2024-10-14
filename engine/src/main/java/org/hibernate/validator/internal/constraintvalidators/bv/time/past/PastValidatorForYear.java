/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.Year;

/**
 * Check that the {@code java.time.Year} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastValidatorForYear extends AbstractPastJavaTimeValidator<Year> {

	@Override
	protected Year getReferenceValue(Clock reference) {
		return Year.now( reference );
	}

}
