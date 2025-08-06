/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
