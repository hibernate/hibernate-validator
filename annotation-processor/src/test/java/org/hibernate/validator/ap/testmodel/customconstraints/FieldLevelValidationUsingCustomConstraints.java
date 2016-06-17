/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
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
