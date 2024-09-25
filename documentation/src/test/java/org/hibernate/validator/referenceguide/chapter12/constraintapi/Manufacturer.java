/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

//end::include[]
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Manufacturer {

	@NotNull
	private String name;

	//...
}
//end::include[]
