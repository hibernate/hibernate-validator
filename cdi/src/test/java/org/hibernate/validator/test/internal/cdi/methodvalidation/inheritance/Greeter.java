/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation.inheritance;

import javax.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public interface Greeter {
	String greet(@Size(max = 5) String greeting);
}
