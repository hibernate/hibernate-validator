/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
