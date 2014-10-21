/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import javax.validation.constraints.Digits;

import org.hibernate.validator.cfg.ConstraintDef;


/**
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
