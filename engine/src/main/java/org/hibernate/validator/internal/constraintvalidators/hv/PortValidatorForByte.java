/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForByte implements ConstraintValidator<Port, Byte> {

	@Override
	public boolean isValid(Byte value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value >= 0;
	}
}
