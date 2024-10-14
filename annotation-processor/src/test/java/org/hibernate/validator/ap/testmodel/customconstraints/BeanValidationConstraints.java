/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.customconstraints;

import java.util.Collection;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class BeanValidationConstraints {

	/**
	 * Allowed.
	 */
	@Email
	@NotEmpty
	@NotBlank
	public String string;

	@Positive
	@PositiveOrZero
	public int number;

	@Negative
	@NegativeOrZero
	public Double otherNumber;

	@NotEmpty
	public List list;

	/**
	 * Not allowed.
	 */
	@Email
	@Negative
	@NegativeOrZero
	@NotEmpty
	@NotBlank
	public Object property;

	@NotEmpty
	@NotBlank
	@Email
	public int badInt;

	@Positive
	@PositiveOrZero
	public Collection collection;
}
