/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

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
