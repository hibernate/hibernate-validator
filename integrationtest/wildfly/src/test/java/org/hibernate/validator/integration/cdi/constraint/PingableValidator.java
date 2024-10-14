/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.cdi.constraint;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.integration.cdi.service.PingService;

/**
 * @author Hardy Ferentschik
 */
public class PingableValidator implements ConstraintValidator<Pingable, String> {
	@Inject
	private PingService pingService;

	@Override
	public void initialize(Pingable constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return true;
	}

	public PingService getPingService() {
		return pingService;
	}
}
