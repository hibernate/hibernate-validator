/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.cdi.configuration;

import java.time.Clock;

import jakarta.inject.Inject;
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
