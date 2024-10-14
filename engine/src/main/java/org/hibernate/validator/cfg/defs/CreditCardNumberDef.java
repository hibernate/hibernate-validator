/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.CreditCardNumber;

/**
 * A {@link CreditCardNumber} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class CreditCardNumberDef extends ConstraintDef<CreditCardNumberDef, CreditCardNumber> {

	public CreditCardNumberDef() {
		super( CreditCardNumber.class );
	}
}
