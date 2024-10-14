/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Past;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class TimeValidatorTest {

	/**
	 * HV-158
	 */
	@Test
	public void testFutureAndPast() {
		Validator validator = ValidatorUtil.getValidator();
		DateHolder dateHolder = new DateHolder();
		Set<ConstraintViolation<DateHolder>> constraintViolations = validator.validate( dateHolder );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Past.class )
		);
	}
}
