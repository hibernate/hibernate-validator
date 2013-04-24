package org.hibernate.validator.referenceguide.chapter03.inheritance.parallel;

import javax.validation.constraints.Max;

public interface Vehicle {

	void drive(@Max(75) int speedInMph);
}