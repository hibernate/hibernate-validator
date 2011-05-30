/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.test.constraints.impl;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.testng.annotations.Test;

import org.hibernate.validator.test.util.ValidatorUtil;

import static org.hibernate.validator.test.util.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.test.util.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * Check correct behavior of {@link org.hibernate.validator.constraints.impl.MinValidatorForNumber} and
 * {@link org.hibernate.validator.constraints.impl.MaxValidatorForNumber} on boundary values.
 * <p/>
 * The chosen numbers: 9223372036854775806l and 9223372036854775807l cast to
 * the same double value.
 *
 * @author Carlos Vara
 * @author Hardy Ferentschik
 */
public class MinMaxValidatorBoundaryTest {
	@Min(value = 9223372036854775807l)
	public long min;

	@Max(value = 9223372036854775806l)
	public long max;

	@Test
	public void testMinBoundaryValue() {
		Validator validator = ValidatorUtil.getValidator();

		this.min = 9223372036854775806l;
		this.max = 0l;

		// Current min value is smaller, should fail, but it doesn't
		Set<ConstraintViolation<MinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test
	public void testMaxBoundaryValue() {
		Validator validator = ValidatorUtil.getValidator();

		this.min = Long.MAX_VALUE;
		this.max = 9223372036854775807l;

		// Current max value is bigger, should fail, but it doesn't
		Set<ConstraintViolation<MinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, Max.class );
	}
}
