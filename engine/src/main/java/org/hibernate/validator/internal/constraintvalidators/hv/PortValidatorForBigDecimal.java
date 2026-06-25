/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForBigDecimal implements ConstraintValidator<Port, BigDecimal> {

	private static final BigDecimal MAX_PORT = BigDecimal.valueOf( 65535 );

	@Override
	public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value.stripTrailingZeros().scale() <= 0
				&& value.compareTo( BigDecimal.ZERO ) >= 0 && value.compareTo( MAX_PORT ) <= 0;
	}
}
