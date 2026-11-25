package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.MaxLines;

public class ModelWithMaxLinesConstraints {

	@MaxLines(5)
	public String string;

	@MaxLines(5)
	public CharSequence charSequence;

	@MaxLines(5)
	public Integer integer;

	@MaxLines(5)
	public int intPrimitive;
}
