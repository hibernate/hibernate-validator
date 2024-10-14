/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
