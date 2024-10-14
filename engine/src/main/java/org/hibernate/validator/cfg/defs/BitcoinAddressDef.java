/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.BitcoinAddress;

/**
 * A {@link BitcoinAddress} constraint definition.
 *
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
