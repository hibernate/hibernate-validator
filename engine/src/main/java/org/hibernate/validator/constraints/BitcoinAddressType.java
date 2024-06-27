/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import java.util.regex.Pattern;

public enum BitcoinAddressType {
	ANY,
	P2PKH( "^(1)[a-zA-HJ-NP-Z0-9]{25,61}$"),
	P2SH( "^(3)[a-zA-HJ-NP-Z0-9]{33}$"),
	BECH32( "^(bc1)[a-zA-HJ-NP-Z0-9]{39,59}$"),
	P2WSH( "^(bc1q)[a-zA-HJ-NP-Z0-9]{58}$"),
	P2WPKH( "^(bc1q)[a-zA-HJ-NP-Z0-9]{38}$"),
	P2TR("^(bc1p)[a-zA-HJ-NP-Z0-9]{58}$");

	private final Pattern pattern;

	BitcoinAddressType(String pattern) {
		this.pattern = pattern != null ? Pattern.compile( pattern ) : null;
	}
	BitcoinAddressType() {
		this( null );
	}

	public Pattern getPattern() {
		return pattern;
	}
}
