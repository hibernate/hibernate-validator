/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.configuration;

import java.time.Clock;

import javax.inject.Inject;
import jakarta.validation.ClockProvider;

import org.hibernate.validator.integration.cdi.service.PingService;

/**
 * @author Guillaume Smet
 */
public class ClockProviderWithInjection implements ClockProvider {
	@Inject
	private PingService pingService;

	@Override
	public Clock getClock() {
		return Clock.systemDefaultZone();
	}

	public PingService getPingService() {
		return pingService;
	}
}
