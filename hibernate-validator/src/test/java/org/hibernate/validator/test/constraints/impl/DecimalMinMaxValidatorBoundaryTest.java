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
import javax.validation.constraints.DecimalMin;

import org.testng.annotations.Test;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DecimalMaxDef;
import org.hibernate.validator.cfg.defs.DecimalMinDef;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * @author Hardy Ferentschik
 */
public class DecimalMinMaxValidatorBoundaryTest {
	public double d;

	@Test
	public void testDecimalMinValue() {
		// use programmatic mapping api to configure constraint
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( DecimalMinMaxValidatorBoundaryTest.class )
				.property( "d", FIELD )
				.constraint( new DecimalMinDef().value( "0.100000000000000005" ) );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		this.d = 0.1;

		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, DecimalMin.class );
	}

	@Test
	public void testDecimalMaxValue() {
		// use programmatic mapping api to configure constraint
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( DecimalMinMaxValidatorBoundaryTest.class )
				.property( "d", FIELD )
				.constraint( new DecimalMaxDef().value( "0.1" ) );

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		this.d = 0.1;

		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 0 );
	}
}
