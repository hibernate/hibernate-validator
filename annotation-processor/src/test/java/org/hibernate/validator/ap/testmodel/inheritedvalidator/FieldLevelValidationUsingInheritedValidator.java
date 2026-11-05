package org.hibernate.validator.ap.testmodel.inheritedvalidator;

import java.util.Date;

public class FieldLevelValidationUsingInheritedValidator {

	@CustomConstraint
	public String string;

	/**
	 * Not allowed.
	 */
	@CustomConstraint
	public Date date;

}
