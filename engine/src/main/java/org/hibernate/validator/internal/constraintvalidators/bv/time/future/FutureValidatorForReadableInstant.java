/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;

import org.joda.time.ReadableInstant;

/**
 * Check if Joda Time type who implements {@code import org.joda.time.ReadableInstant} is in the future.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class FutureValidatorForReadableInstant extends AbstractFutureEpochBasedValidator<ReadableInstant> {

	@Override
	protected long getEpochMillis(ReadableInstant value, Clock reference) {
		return value.getMillis();
	}

}
