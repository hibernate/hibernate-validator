/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.objectgraph.containerelement;

//end::include[]
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Car {

	private List<@NotNull @Valid Person> passengers = new ArrayList<Person>();

	private Map<@Valid Part, List<@Valid Manufacturer>> partManufacturers = new HashMap<>();

	//...
}
//end::include[]
