/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.ISBN;

/**
 * @author Marko Bekhta
 */
public class ModelWithISBNConstraints {

	@ISBN
	private String string;

	@ISBN
	private CharSequence charSequence;

	@ISBN
	private Integer integer;

}
