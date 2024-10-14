/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
