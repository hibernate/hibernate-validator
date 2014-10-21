/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import javax.validation.Valid;


/**
 * @author Gunnar Morling
 */
public class ChildWithAtValid extends ParentWithoutAtValid {

	@Valid
	public Order getOrder() {
		return null;
	}

}
