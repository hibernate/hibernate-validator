//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.nested;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

//tag::include[]
public class Car {

	private Map<@NotNull Part, List<@NotNull Manufacturer>> partManufacturers =
			new HashMap<>();

	//...
}
//end::include[]
