/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
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
