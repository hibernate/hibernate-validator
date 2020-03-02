/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.configuration;

import javax.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

import org.hibernate.validator.integration.cdi.service.PingService;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorFactoryWithInjection implements ConstraintValidatorFactory {
	@Inject
	private PingService pingService;

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		return null;
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
	}

	public PingService getPingService() {
		return pingService;
	}
}
