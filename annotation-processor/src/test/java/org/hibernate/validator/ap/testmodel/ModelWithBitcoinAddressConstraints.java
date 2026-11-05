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
