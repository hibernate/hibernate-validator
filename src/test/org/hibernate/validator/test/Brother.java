//$Id$
package org.hibernate.validator.test;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;

/**
 * @author Emmanuel Bernard
 */
public class Brother {
	private String name;
	private Brother elder;
	private Brother youngerBrother;
	@NotNull
	@Valid
	private Address address;

	@NotNull
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	@Valid
	public Brother getElder() {
		return elder;
	}

	public void setElder(Brother elder) {
		this.elder = elder;
	}

	@Valid
	public Brother getYoungerBrother() {
		return youngerBrother;
	}

	public void setYoungerBrother(Brother youngerBrother) {
		this.youngerBrother = youngerBrother;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public boolean equals(Object o) {
		return true; //workaround dummy equals and hashcode?
	}

	public int hashCode() {
		return 5;
	}
}
