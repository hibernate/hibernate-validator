/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.AbstractMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Check correct behavior of {@link MinValidatorForNumber} and
 * {@link AbstractMaxValidator} on boundary values.
 * <p/>
 * The chosen numbers: 9223372036854775806l and 9223372036854775807l cast to
 * the same double value.
 *
 * @author Carlos Vara
 * @author Hardy Ferentschik
 */
public class MinMaxValidatorBoundaryTest {

	@Min(value = 9223372036854775807L)
	public long min;

	@Max(value = 9223372036854775806L)
	public long max;

	@Test
	public void testMinBoundaryValue() {
		Validator validator = ValidatorUtil.getValidator();

		this.min = 9223372036854775806L;
		this.max = 0L;

		// Current min value is smaller, should fail, but it doesn't
		Set<ConstraintViolation<MinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
		);
	}

	@Test
	public void testMaxBoundaryValue() {
		Validator validator = ValidatorUtil.getValidator();

		this.min = Long.MAX_VALUE;
		this.max = 9223372036854775807L;

		// Current max value is bigger, should fail, but it doesn't
		Set<ConstraintViolation<MinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class )
		);
	}
}
