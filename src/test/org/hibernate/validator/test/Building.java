//$Id: $
package org.hibernate.validator.test;

import org.hibernate.validator.Length;

/**
 * @author Emmanuel Bernard
 */
public class Building {
	private Long id;

	@Length( min = 1, message = "{notpresent.Key} and #{key.notPresent} and {key.notPresent2} {min}" )
	private String address;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}

