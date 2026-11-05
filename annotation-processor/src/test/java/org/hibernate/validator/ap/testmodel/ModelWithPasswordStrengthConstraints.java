package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.PasswordStrength;

public class ModelWithPasswordStrengthConstraints {

	@PasswordStrength(min = 3)
	private String string;

	@PasswordStrength(min = 3)
	private char[] charArray;

	@PasswordStrength(min = 3)
	private Integer integer;
}
