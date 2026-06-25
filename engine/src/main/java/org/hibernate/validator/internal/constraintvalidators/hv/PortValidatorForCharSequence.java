/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForCharSequence implements ConstraintValidator<Port, CharSequence> {

	private static final BigDecimal MAX_PORT = BigDecimal.valueOf( 65535 );

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		try {
			BigDecimal bd = new BigDecimal( value.toString() );
			return bd.stripTrailingZeros().scale() <= 0
					&& bd.compareTo( BigDecimal.ZERO ) >= 0 && bd.compareTo( MAX_PORT ) <= 0;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}
}
