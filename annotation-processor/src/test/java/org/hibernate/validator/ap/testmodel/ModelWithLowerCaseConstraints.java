package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.LowerCase;

public class ModelWithLowerCaseConstraints {

	@LowerCase
	public String string;

	@LowerCase
	public CharSequence charSequence;

	@LowerCase
	public Integer integer;

	@LowerCase
	public int intPrimitive;
}
