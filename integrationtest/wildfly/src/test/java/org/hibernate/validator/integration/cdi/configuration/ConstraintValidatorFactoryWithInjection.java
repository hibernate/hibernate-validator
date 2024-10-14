/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.cdi.configuration;

import jakarta.inject.Inject;
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
