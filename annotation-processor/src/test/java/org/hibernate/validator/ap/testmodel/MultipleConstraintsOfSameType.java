/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Pattern.List;

public class MultipleConstraintsOfSameType {

	@List(value = { @Pattern(regexp = ""), @Pattern(regexp = "") })
	public String string;

	/**
	 * Not allowed.
	 */
	@List(value = { @Pattern(regexp = ""), @Pattern(regexp = "") })
	public Date date;
}
