/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForLong implements ConstraintValidator<Port, Long> {

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value >= 0 && value <= 65535;
	}
}
