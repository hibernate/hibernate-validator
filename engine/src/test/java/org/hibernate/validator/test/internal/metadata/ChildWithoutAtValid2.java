/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class ChildWithoutAtValid2 extends ParentWithoutAtValid {

	@Override
	@NotNull
	public Order getOrder() {
		return null;
	}

}
