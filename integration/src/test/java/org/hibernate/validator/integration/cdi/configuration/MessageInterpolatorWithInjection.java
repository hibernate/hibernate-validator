/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.configuration;

import java.util.Locale;
import javax.inject.Inject;
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
