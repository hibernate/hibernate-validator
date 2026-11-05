package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.IpAddress;

/**
 * @author Ivan Malutin
 */
public class ModelWithIpAddressConstraints {

	@IpAddress
	private String string;

	@IpAddress
	private CharSequence charSequence;

	@IpAddress
	private Integer integer;
}
