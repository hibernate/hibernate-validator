/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.proxy;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

interface A {
	@Min(5)
	Integer getInteger();

	@Size(min = 2)
	String getString();
}
