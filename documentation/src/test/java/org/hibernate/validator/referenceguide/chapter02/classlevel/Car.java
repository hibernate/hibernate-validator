/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
