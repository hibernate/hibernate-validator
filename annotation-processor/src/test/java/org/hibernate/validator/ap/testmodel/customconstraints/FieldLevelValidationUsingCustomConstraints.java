package org.hibernate.validator.ap.testmodel.customconstraints;

import java.util.Date;

public class FieldLevelValidationUsingCustomConstraints {

	@CheckCase(CaseMode.UPPER)
	public String string;

	/**
	 * Not allowed.
	 */
	@CheckCase(CaseMode.UPPER)
	public Date date;

}
