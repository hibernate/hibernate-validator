/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForNumber implements ConstraintValidator<Port, Number> {

	@Override
	public boolean isValid(Number value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		if ( value instanceof BigDecimal bd ) {
			return bd.stripTrailingZeros().scale() <= 0
					&& bd.compareTo( BigDecimal.ZERO ) >= 0 && bd.compareTo( BigDecimal.valueOf( 65535 ) ) <= 0;
		}
		if ( value instanceof BigInteger bi ) {
			return bi.compareTo( BigInteger.ZERO ) >= 0 && bi.compareTo( BigInteger.valueOf( 65535 ) ) <= 0;
		}
		if ( value instanceof Double d ) {
			if ( Double.isNaN( d ) || Double.isInfinite( d ) ) {
				return false;
			}
			return d % 1 == 0 && d >= 0 && d <= 65535;
		}
		if ( value instanceof Float f ) {
			if ( Float.isNaN( f ) || Float.isInfinite( f ) ) {
				return false;
			}
			return f % 1 == 0 && f >= 0 && f <= 65535;
		}
		long l = value.longValue();
		return l >= 0 && l <= 65535;
	}
}
