/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.crossparameters;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GenericNormalValidator implements ConstraintValidator<Annotation, Object> {

	@Override
	public void initialize(final Annotation constraintAnnotation) {
	}

	@Override
	public boolean isValid(final Object value, final ConstraintValidatorContext context) {
		// some validation logic
		return true;
	}

}
