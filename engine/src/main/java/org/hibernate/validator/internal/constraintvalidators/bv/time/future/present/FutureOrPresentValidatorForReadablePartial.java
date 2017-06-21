/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future.present;

import java.time.Clock;

import org.joda.time.Instant;
import org.joda.time.ReadablePartial;

/**
 * Check if Joda Time type who implements {@code org.joda.time.ReadablePartial} is in the future.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForReadablePartial extends AbstractFutureOrPresentEpochBasedValidator<ReadablePartial> {

	@Override
	protected long getEpochMillis(ReadablePartial value, Clock reference) {
		return value.toDateTime( new Instant( reference.millis() ) ).getMillis();
	}

}
