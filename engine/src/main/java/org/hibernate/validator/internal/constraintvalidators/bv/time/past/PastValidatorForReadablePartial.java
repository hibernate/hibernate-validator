/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;

import org.joda.time.Instant;
import org.joda.time.ReadablePartial;

/**
 * Check if Joda Time type who implements {@code org.joda.time.ReadablePartial} is in the past.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class PastValidatorForReadablePartial extends AbstractPastEpochBasedValidator<ReadablePartial> {

	@Override
	protected long getEpochMillis(ReadablePartial value, Clock reference) {
		return value.toDateTime( new Instant( reference.millis() ) ).getMillis();
	}

}
