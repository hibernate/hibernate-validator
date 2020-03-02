/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.customconstraints;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.valueextraction.Unwrapping;

public class UnwrappingConstraints {

	/**
	 * Allowed.
	 */
	@Min(value = 5)
	public OptionalInt optionalInt;

	@Min(value = 5)
	public OptionalLong optionalLong;

	@Min(value = 5)
	public OptionalDouble optionalDouble;
	/**
	 * Not allowed.
	 */
	@Past
	public OptionalInt badOptionalInt;

	@Past
	public OptionalLong badOptionalLong;

	@Past
	public OptionalDouble badOptionalDouble;

	@Min(value = 10, payload = Unwrapping.Skip.class)
	public OptionalInt skipOptionalInt;

	@Min(value = 10, payload = Unwrapping.Skip.class)
	public OptionalLong skipOptionalLong;

	@Min(value = 10, payload = Unwrapping.Skip.class)
	public OptionalDouble skipOptionalDouble;

	/**
	 * Warning
	 */
	@Min(value = 10, payload = Unwrapping.Unwrap.class)
	public Integer unwrappedInteger;
}
