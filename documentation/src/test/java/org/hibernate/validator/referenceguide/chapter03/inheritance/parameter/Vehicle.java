/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.inheritance.parameter;

//end::include[]
import jakarta.validation.constraints.Max;

//tag::include[]
public interface Vehicle {

	void drive(@Max(75) int speedInMph);
}
//end::include[]
