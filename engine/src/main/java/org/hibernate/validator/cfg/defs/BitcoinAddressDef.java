/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.constraints.BitcoinAddressType;
import org.hibernate.validator.constraints.ISBN;

/**
 * @author José Yoshiriro
 * @since 8.0.2
 */
public class BitcoinAddressDef extends ConstraintDef<BitcoinAddressDef, BitcoinAddress> {

	public BitcoinAddressDef() {
		super( BitcoinAddress.class );
	}

	public BitcoinAddressDef value(BitcoinAddressType type) {
		addParameter( "value", new BitcoinAddressType[] { type } );
		return this;
	}
}
