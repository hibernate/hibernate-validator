/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

public enum BitcoinAddressType {
	ANY("Bitcoin", null),
	P2PKH("Legacy (P2PKH)", "^(1)[a-zA-HJ-NP-Z0-9]{25,61}$"),
	P2SH("Nested SegWit (P2SH)", "^(3)[a-zA-HJ-NP-Z0-9]{33}$"),
	BECH32("Native SegWit (Bech32)", "^(bc1)[a-zA-HJ-NP-Z0-9]{39,59}$"),
	P2WSH("SegWit variant of P2SH (P2WSH)", "^(bc1q)[a-zA-HJ-NP-Z0-9]{58}$"),
	P2WPKH("SegWit variant of P2PKH (P2WPKH)", "^(bc1q)[a-zA-HJ-NP-Z0-9]{38}$"),
	P2TR("Taproot (P2TR)", "^(bc1p)[a-zA-HJ-NP-Z0-9]{58}$");

	private final String description;
	private final String regex;

	BitcoinAddressType(String description, String regex) {
		this.description = description;
		this.regex = regex;
	}

	public String getDescription() {
		return description;
	}

	public String getRegex() {
		return regex;
	}

	@Override
	public String toString() {
		return this.description;
	}
}
