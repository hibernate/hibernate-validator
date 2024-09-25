/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter10;

//end::include[]
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@SuppressWarnings("unused")
//tag::include[]
public class Library {

	@NotNull
	private String name;

	private List<@NotNull @Valid Book> books;

	//getters and setters ...
}
//end::include[]
