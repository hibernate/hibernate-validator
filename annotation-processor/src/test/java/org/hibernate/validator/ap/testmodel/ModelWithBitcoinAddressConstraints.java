/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.BitcoinAddress;

/**
 * @author José Yoshiriro
 */
public class ModelWithBitcoinAddressConstraints {

	@BitcoinAddress
	private String string;

	@BitcoinAddress
	private Integer integer;

	@BitcoinAddress
	private Boolean aBoolean;

}
