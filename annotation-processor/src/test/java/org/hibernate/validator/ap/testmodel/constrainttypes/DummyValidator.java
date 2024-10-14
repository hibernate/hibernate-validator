/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.constrainttypes;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Gunnar Morling
 */
public class DummyValidator implements ConstraintValidator<Annotation, Object> {

	@Override
	public void initialize(Annotation constraintAnnotation) {
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		throw new UnsupportedOperationException( "Not implemented" );
	}

}
