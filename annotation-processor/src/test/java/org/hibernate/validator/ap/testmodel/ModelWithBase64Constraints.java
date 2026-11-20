package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.Base64;

public class ModelWithBase64Constraints {

	@Base64
	public String string;

	@Base64
	public CharSequence charSequence;

	@Base64
	public Integer integer;

	@Base64
	public int intPrimitive;
}
