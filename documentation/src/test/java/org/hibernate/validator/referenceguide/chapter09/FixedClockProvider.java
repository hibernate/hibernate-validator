/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import java.time.Clock;
import java.time.ZonedDateTime;

import jakarta.validation.ClockProvider;

//tag::include[]
public class FixedClockProvider implements ClockProvider {

	private Clock clock;

	public FixedClockProvider(ZonedDateTime dateTime) {
		clock = Clock.fixed( dateTime.toInstant(), dateTime.getZone() );
	}

	@Override
	public Clock getClock() {
		return clock;
	}

}
//end::include[]
