/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.inheritance.returnvalue;

//end::include[]
import java.util.List;

import jakarta.validation.constraints.NotNull;

//tag::include[]
public interface Vehicle {

	@NotNull
	List<Person> getPassengers();
}
//end::include[]
