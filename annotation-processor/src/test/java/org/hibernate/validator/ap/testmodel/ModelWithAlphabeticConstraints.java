/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.Alphabetic;

public class ModelWithAlphabeticConstraints {

	@Alphabetic
	public String string;

	@Alphabetic
	public CharSequence charSequence;

	@Alphabetic
	public Integer integer;

	@Alphabetic
	public int intPrimitive;
}
