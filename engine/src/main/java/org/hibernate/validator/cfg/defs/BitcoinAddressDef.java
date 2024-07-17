/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.BitcoinAddress;

/**
 * @author José Yoshiriro
 * @since 9.0.0
 */
@Incubating
public class BitcoinAddressDef extends ConstraintDef<BitcoinAddressDef, BitcoinAddress> {

	public BitcoinAddressDef() {
		super( BitcoinAddress.class );
	}

	public BitcoinAddressDef value(BitcoinAddress.BitcoinAddressType... type) {
		addParameter( "value", type );
		return this;
	}
}
