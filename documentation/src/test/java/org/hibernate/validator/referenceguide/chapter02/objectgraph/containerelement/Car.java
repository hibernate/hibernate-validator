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
