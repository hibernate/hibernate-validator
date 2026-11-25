package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.Finite;

public class ModelWithFiniteConstraints {

	@Finite
	public String string;

	@Finite
	public Integer integer;

	@Finite
	public Double doubleValue;

	@Finite
	public Float floatValue;

	@Finite
	public double primitiveDouble;

	@Finite
	public float primitiveFloat;
}
