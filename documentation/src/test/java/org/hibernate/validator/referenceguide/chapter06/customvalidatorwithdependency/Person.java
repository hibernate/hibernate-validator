//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.customvalidatorwithdependency;

public class Person {

	@ZipCode
	private String zipCode;

	public Person(String zipCode) {
		this.zipCode = zipCode;
	}
}
//end::include[]
