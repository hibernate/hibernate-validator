/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

/**
 * @author Gunnar Morling
 */
@SupportedValidationTarget(value = ValidationTarget.PARAMETERS)
public class ConsistentDateParametersValidator implements ConstraintValidator<ConsistentDateParameters, Object[]> {

	@Override
	public boolean isValid(Object[] value, ConstraintValidatorContext context) {
		if ( value.length != 2 ) {
			throw new IllegalArgumentException( "Unexpected method signature" );
		}

		if ( value[0] == null || value[1] == null ) {
			return true;
		}

		if ( !( value[0] instanceof LocalDate ) || !( value[1] instanceof LocalDate ) ) {
			throw new IllegalArgumentException( "Unexpected method signature" );
		}

		return ( (LocalDate) value[0] ).isBefore( (LocalDate) value[1] );
	}
}
