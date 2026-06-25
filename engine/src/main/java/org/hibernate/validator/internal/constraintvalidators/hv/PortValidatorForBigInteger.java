/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigInteger;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Port;

public class PortValidatorForBigInteger implements ConstraintValidator<Port, BigInteger> {

	private static final BigInteger MAX_PORT = BigInteger.valueOf( 65535 );

	@Override
	public boolean isValid(BigInteger value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value.compareTo( BigInteger.ZERO ) >= 0 && value.compareTo( MAX_PORT ) <= 0;
	}
}
