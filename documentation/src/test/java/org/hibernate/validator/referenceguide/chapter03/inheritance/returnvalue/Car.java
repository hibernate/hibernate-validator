/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.inheritance.returnvalue;

//end::include[]
import java.util.List;

import jakarta.validation.constraints.Size;

//tag::include[]
public class Car implements Vehicle {

	@Override
	@Size(min = 1)
	public List<Person> getPassengers() {
		//...
		return null;
	}
}
//end::include[]
