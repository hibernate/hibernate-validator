/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation;

import javax.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution
public class DefaultRepeater implements Repeater<String> {

	@Override
	public String repeat(String in) {
		return in;
	}

	@Override
	public String reverse(String in) {
		return null;
	}

	@Override
	public String getHelloWorld() {
		return null;
	}
}
