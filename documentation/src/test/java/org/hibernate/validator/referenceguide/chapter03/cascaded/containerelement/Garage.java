/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.cascaded.containerelement;

//end::include[]
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Garage {

	public boolean checkCars(@NotNull List<@Valid Car> cars) {
		//...
		return false;
	}
}
//end::include[]
