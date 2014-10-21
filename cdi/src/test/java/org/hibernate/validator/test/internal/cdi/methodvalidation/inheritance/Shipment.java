/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation.inheritance;

import javax.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class Shipment {

	@NotNull
	private String name;

	public String getName() {
		return name;
	}
}
