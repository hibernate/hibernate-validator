/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.crossparameter;

//end::include[]
import java.util.List;

//tag::include[]
public class Car {

	@LuggageCountMatchesPassengerCount(piecesOfLuggagePerPassenger = 2)
	public void load(List<Person> passengers, List<PieceOfLuggage> luggage) {
		//...
	}
}
//end::include[]
