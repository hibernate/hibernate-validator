//$Id$
package org.hibernate.validator.test;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;

/**
 * @author Emmanuel Bernard
 */
public class Site {
	@NotNull
	private String siteName;

	@Valid
	private Address address;

	@Valid
	private Contact contact;

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
}