/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.validationordergenerator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class ZipCodeCoherenceValidator implements ConstraintValidator<ZipCodeCoherenceChecker, Address> {

	@Override
	public boolean isValid(Address value, ConstraintValidatorContext constraintValidatorContext) {
		return false;
	}
}
