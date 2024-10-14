/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Currency;

/**
 * A {@link Currency} constraint definition.
 *
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
