package org.hibernate.validation.engine.xml;

/**
 * @author Hardy Ferentschik
 */
public class User {

	private boolean isConsistent;

	private String firstname;

	//@NotNull(groups = Default.class)
	private String lastname;

	//@Pattern(regexp = "[0-9 -]?", groups = Optional.class)
	private String phoneNumber;


	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setConsistent(boolean consistent) {
		isConsistent = consistent;
	}

	public boolean isConsistent() {
		return isConsistent;
	}
}