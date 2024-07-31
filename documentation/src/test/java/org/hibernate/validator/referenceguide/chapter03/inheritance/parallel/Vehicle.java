//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.inheritance.parallel;

//end::include[]
import jakarta.validation.constraints.Max;

//tag::include[]
public interface Vehicle {

	void drive(@Max(75) int speedInMph);
}
//end::include[]
