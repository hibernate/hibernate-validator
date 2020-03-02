/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import jakarta.validation.constraints.Min;

public class Review {

	@Min(0)
	private int rating;

	public Review(int rating) {
		this.rating = rating;
	}

	// [...]
}
