/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter04.resourcebundlelocator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public class Car {

	@NotNull
	private String licensePlate;

	@Max(300)
	private int topSpeed = 400;

}
