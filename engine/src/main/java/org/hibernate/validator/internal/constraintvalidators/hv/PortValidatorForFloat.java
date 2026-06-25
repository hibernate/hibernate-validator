/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForFloat implements ConstraintValidator<Port, Float> {

	@Override
	public boolean isValid(Float value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		if ( Float.isNaN( value ) || Float.isInfinite( value ) ) {
			return false;
		}
		return value % 1 == 0 && value >= 0 && value <= 65535;
	}
}
