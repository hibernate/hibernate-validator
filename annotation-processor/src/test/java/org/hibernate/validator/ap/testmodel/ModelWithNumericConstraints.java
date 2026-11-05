/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.Numeric;

public class ModelWithNumericConstraints {

	@Numeric
	public String string;

	@Numeric
	public CharSequence charSequence;

	@Numeric
	public Integer integer;

	@Numeric
	public int intPrimitive;
}
