/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;

public class ModelWithDateConstraints {

	/**
	 * Not allowed.
	 */
	@Past
	@FutureOrPresent
	public String string;

	@Past
	@FutureOrPresent
	public Date date;
}
