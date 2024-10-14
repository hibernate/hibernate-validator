/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine;

import java.time.Clock;

import jakarta.validation.ClockProvider;

/**
 * A default {@link ClockProvider} implementation which returns the current system time in the default time zone using
 * {@link Clock#systemDefaultZone()}.
 *
 * @author Guillaume Smet
 */
public class DefaultClockProvider implements ClockProvider {

	public static final DefaultClockProvider INSTANCE = new DefaultClockProvider();

	private DefaultClockProvider() {
	}

	@Override
	public Clock getClock() {
		return Clock.systemDefaultZone();
	}

}
