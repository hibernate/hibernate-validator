/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter10;

//end::include[]
import jakarta.validation.constraints.NotEmpty;

//tag::include[]
public class Book {

	@NotEmpty
	private String title;

	@NotEmpty
	private String author;

	//getters and setters ...
}
//end::include[]
