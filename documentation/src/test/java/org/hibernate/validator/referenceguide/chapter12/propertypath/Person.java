/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter12.propertypath;

import jakarta.validation.constraints.Size;

public class Person {

	@Size(min = 5)
	String name;

	Person(String name) {
		this.name = name;
	}
}
