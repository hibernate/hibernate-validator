/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

public class ModelWithoutConstraints {

	@SomeAnnotation
	public String string;

	private @interface SomeAnnotation {
	}
}
