/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Currency;

/**
 * @author Gunnar Morling
 */
public class CurrencyDef extends ConstraintDef<CurrencyDef, Currency> {

	public CurrencyDef() {
		super( Currency.class );
	}

	public CurrencyDef value(String... value) {
		addParameter( "value", value );
		return this;
	}
}
