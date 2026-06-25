/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForDouble implements ConstraintValidator<Port, Double> {

	@Override
	public boolean isValid(Double value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		if ( Double.isNaN( value ) || Double.isInfinite( value ) ) {
			return false;
		}
		return value % 1 == 0 && value >= 0 && value <= 65535;
	}
}
