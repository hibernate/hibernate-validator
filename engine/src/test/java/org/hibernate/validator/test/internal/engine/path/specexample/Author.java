/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@SecurityChecking
public class Author {

	private String firstName;

	@NotEmpty(message = "lastname must not be null")
	private String lastName;

	@Size(max = 30)
	private String company;

	@OldAndNewPasswordsDifferent
	@NewPasswordsIdentical
	public void renewPassword(String oldPassword, String newPassword, String retypedNewPassword) {
	}

	// [...]

	public Author(String lastName) {
		this.lastName = lastName;
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
}
