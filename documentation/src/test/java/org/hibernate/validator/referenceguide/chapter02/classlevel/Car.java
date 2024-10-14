/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.classlevel;

//end::include[]
import java.util.List;

//tag::include[]
@ValidPassengerCount
public class Car {

	private int seatCount;

	private List<Person> passengers;

	//...
}
//end::include[]
