/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.customconstraints;

import java.util.Collection;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

public class BeanValidationConstraints {

	/**
	 * Allowed.
	 */
	@Email
	@NotEmpty
	@NotBlank
	public String string;

	@Positive
	public int number;

	@Negative
	public Double otherNumber;

	@NotEmpty
	public List list;

	/**
	 * Not allowed.
	 */
	@Email
	@Negative
	@NotEmpty
	@NotBlank
	public Object property;

	@NotEmpty
	@NotBlank
	@Email
	public int badInt;

	@Positive
	public Collection collection;
}
