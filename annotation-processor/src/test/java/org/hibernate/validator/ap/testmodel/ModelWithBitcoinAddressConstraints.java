/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.constraints.ISBN;

/**
 * @author José Yoshiriro
 */
public class ModelWithBitcoinAddressConstraints {

	@BitcoinAddress
	private String string;

	@ISBN
	private Integer integer;

	@ISBN
	private Boolean aBoolean;

}
