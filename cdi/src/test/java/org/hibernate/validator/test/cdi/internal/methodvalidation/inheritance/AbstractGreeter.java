/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

/**
 * @author Hardy Ferentschik
 */
public abstract class AbstractGreeter implements Greeter {
	@Override
	public String greet(String greeting) {
		return "Hello" + getName() + ", " + greeting;
	}

	protected abstract String getName();
}
