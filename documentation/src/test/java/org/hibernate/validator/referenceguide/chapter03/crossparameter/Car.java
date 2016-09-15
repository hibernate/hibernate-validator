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
