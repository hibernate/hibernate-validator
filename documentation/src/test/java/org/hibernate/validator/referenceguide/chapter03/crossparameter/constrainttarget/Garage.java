/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.crossparameter.constrainttarget;

//end::include[]
import java.util.List;

import jakarta.validation.ConstraintTarget;

//tag::include[]
public class Garage {

	@ELAssert(expression = "...", validationAppliesTo = ConstraintTarget.PARAMETERS)
	public Car buildCar(List<Part> parts) {
		//...
		return null;
	}

	@ELAssert(expression = "...", validationAppliesTo = ConstraintTarget.RETURN_VALUE)
	public Car paintCar(int color) {
		//...
		return null;
	}
}
//end::include[]
