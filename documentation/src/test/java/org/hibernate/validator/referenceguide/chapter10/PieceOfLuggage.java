/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter10;

import jakarta.validation.constraints.NotNull;

public class PieceOfLuggage {

	@NotNull
	private String name;

	//getters and setters ...
}
