/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.List;

public class MultipleConstraintsOfSameType {

	@List(value = { @Pattern(regexp = ""), @Pattern(regexp = "") })
	public String string;

	/**
	 * Not allowed.
	 */
	@List(value = { @Pattern(regexp = ""), @Pattern(regexp = "") })
	public Date date;
}
