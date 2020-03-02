/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;

public class ModelWithDateConstraints {

	/**
	 * Not allowed.
	 */
	@Past
	@FutureOrPresent
	public String string;

	@Past
	@FutureOrPresent
	public Date date;
}
