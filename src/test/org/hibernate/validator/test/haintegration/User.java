//$Id: $
package org.hibernate.validator.test.haintegration;

import javax.persistence.Embeddable;

import org.hibernate.validator.NotNull;

/**
 * @author Emmanuel Bernard
 */
@Embeddable
public class User {
	@NotNull
	private String firstname;
	private String middlename;
	@NotNull
	private String lastname;

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

	public String getMiddlename() {
		return middlename;
	}

	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}
}
