/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
public class Snafu {
	@ValidateOnExecution(type = { })
	@NotNull
	public String foo() {
		return null;
	}
}
