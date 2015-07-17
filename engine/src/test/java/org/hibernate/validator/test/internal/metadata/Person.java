/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Hardy Ferentschik
 */
public interface Person {
	@NotEmpty(groups = PersonValidation.class)
	String getFirstName();

	String getMiddleName();

	@NotEmpty
	String getLastName();

	public interface PersonValidation {
	}
}
