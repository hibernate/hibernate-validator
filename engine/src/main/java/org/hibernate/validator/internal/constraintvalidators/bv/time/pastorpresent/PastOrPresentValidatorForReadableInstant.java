/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;

import org.joda.time.ReadableInstant;

/**
 * Check if Joda Time type who implements {@code import org.joda.time.ReadableInstant} is in the past.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForReadableInstant extends AbstractPastOrPresentEpochBasedValidator<ReadableInstant> {

	@Override
	protected long getEpochMillis(ReadableInstant value, Clock reference) {
		return value.getMillis();
	}

}
