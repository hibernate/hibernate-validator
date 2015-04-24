/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.generictype;

import javax.validation.constraints.NotNull;

public interface GenericInterface<T> {
	void genericArg(@NotNull T arg);
}
