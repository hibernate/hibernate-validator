//$Id: $
package org.hibernate.validator.test.valid;

import org.hibernate.validator.Valid;

/**
 * @author Emmanuel Bernard
 */
public class Member {

	private Address address;

	@Valid
	public Address getAddress() {
		return address;
	}


	public void setAddress(Address address) {
		this.address = address;
	}
}
