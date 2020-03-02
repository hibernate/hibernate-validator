/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import jakarta.validation.constraints.Size;

@SecurityChecking
public class Author {

	private String firstName;

	@NonEmpty(message = "lastname must not be null")
	private String lastName;

	@Size(max = 30)
	private String company;

	private boolean securityClearance;

	@OldAndNewPasswordsDifferent
	@NewPasswordsIdentical
	public void renewPassword(String oldPassword, String newPassword, String retypedNewPassword) {
	}

	// [...]

	public Author(String lastName) {
		this.lastName = lastName;
		this.securityClearance = true;
	}

	public Author(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.securityClearance = true;
	}

	public Author(String lastName, boolean securityClearance) {
		this.lastName = lastName;
		this.securityClearance = securityClearance;
	}

	public Author(String firstName, String lastName, String company) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.company = company;
		this.securityClearance = true;
	}

	public Author(String firstName, String lastName, String company, boolean securityClearance) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.company = company;
		this.securityClearance = securityClearance;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public boolean hasSecurityClearance() {
		return securityClearance;
	}

	public void setSecurityClearance(boolean securityClearance) {
		this.securityClearance = securityClearance;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "Author(" );
		sb.append( "firstName=" ).append( firstName ).append( ", " );
		sb.append( "lastName=" ).append( lastName );
		sb.append( ")" );
		return sb.toString();
	}
}
