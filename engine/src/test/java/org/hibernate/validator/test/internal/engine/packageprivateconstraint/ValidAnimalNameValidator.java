/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
