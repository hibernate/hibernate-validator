/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.URI;

/**
 * Test model for {@link URI} constraint annotation processor tests.
 *
 * @author Andrea Boriero
 */
public class ModelWithURIConstraints {

	/**
	 * Valid: URI constraint on CharSequence (String)
	 */
	@URI
	public String validURI;

	/**
	 * Invalid: URI constraint on Integer
	 */
	@URI
	public Integer invalidURI;
}
