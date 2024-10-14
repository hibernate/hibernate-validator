/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter10;

//end::include[]
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Person {

	public interface Basic {
	}

	@NotNull
	private String name;

	//getters and setters ...
}
//end::include[]
