/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.Alphanumeric;

public class ModelWithAlphanumericConstraints {

	@Alphanumeric
	public String string;

	@Alphanumeric
	public CharSequence charSequence;

	@Alphanumeric
	public Integer integer;

	@Alphanumeric
	public int intPrimitive;
}
