/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.packageprivateconstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Gunnar Morling
 */
public class ValidAnimalNameValidator implements ConstraintValidator<ValidAnimalName, String> {

	private String name;

	@Override
	public void initialize(ValidAnimalName constraintAnnotation) {
		name = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value == null || value.equals( name );
	}

}
