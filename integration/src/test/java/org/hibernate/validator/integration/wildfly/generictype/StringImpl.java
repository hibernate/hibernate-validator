/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.generictype;

import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

@ValidateOnExecution(type = ExecutableType.ALL)
public class StringImpl implements StringInterface {
	@Override
	public void genericArg(String arg) {
	}
}
