//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.getterselectionstrategy;

//end::include[]
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

//tag::include[]
public class User {

	private String firstName;
	private String lastName;
	private String email;

	// [...]

	//end::include[]
	public User(String firstName, String lastName, String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}

	//tag::include[]
	@NotEmpty
	public String firstName() {
		return firstName;
	}

	@NotEmpty
	public String lastName() {
		return lastName;
	}

	@Email
	public String email() {
		return email;
	}
}
//end::include[]
