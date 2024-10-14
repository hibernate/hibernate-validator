/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.cdi.configuration;

import java.util.Locale;

import jakarta.inject.Inject;
import jakarta.validation.MessageInterpolator;

import org.hibernate.validator.integration.cdi.service.PingService;

/**
 * @author Hardy Ferentschik
 */
public class MessageInterpolatorWithInjection implements MessageInterpolator {
	@Inject
	private PingService pingService;

	@Override
	public String interpolate(String messageTemplate, Context context) {
		return null;
	}

	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale) {
		return null;
	}

	public PingService getPingService() {
		return pingService;
	}
}
