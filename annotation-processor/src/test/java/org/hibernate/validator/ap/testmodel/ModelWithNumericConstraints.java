package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.Numeric;

public class ModelWithNumericConstraints {

	@Numeric
	public String string;

	@Numeric
	public CharSequence charSequence;

	@Numeric
	public Integer integer;

	@Numeric
	public int intPrimitive;
}
