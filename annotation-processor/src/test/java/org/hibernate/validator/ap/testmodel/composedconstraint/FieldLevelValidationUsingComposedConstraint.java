package org.hibernate.validator.ap.testmodel.composedconstraint;

import java.util.Date;

public class FieldLevelValidationUsingComposedConstraint {
	@ValidOrderNumber
	public String string;

	/**
	 * Not allowed.
	 */
	@ValidOrderNumber
	public Date date;
}
