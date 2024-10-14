/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cfg;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class MarathonConstraintValidator implements ConstraintValidator<MarathonConstraint, Marathon> {
	private int minRunners;

	@Override
	public void initialize(MarathonConstraint constraintAnnotation) {
		minRunners = constraintAnnotation.minRunner();
	}

	@Override
	public boolean isValid(Marathon m, ConstraintValidatorContext context) {
		return m.getRunners().size() >= minRunners;
	}
}
