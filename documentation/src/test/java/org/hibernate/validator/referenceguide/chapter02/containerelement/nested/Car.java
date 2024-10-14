/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.nested;

//end::include[]
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Car {

	private Map<@NotNull Part, List<@NotNull Manufacturer>> partManufacturers =
			new HashMap<>();

	//...
}
//end::include[]
