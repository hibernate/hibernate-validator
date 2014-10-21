/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
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
