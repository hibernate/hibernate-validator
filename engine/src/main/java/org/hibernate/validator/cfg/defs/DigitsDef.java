/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Digits;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Digits} constraint definition.
 * @author Hardy Ferentschik
 */
public class DigitsDef extends ConstraintDef<DigitsDef, Digits> {

	public DigitsDef() {
		super( Digits.class );
	}

	public DigitsDef integer(int integer) {
		addParameter( "integer", integer );
		return this;
	}

	public DigitsDef fraction(int fraction) {
		addParameter( "fraction", fraction );
		return this;
	}
}
