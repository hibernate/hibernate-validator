/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DecimalMaxDef;
import org.hibernate.validator.cfg.defs.DecimalMinDef;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class DecimalMinMaxValidatorBoundaryTest {
	private Double d;
	private HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	public void testDecimalMinValue() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( DecimalMinMaxValidatorBoundaryTest.class )
				.field( "d" )
				.constraint( new DecimalMinDef().value( "0.100000000000000005" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		this.d = 0.1;

		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DecimalMin.class )
		);
	}

	@Test
	public void testDecimalMaxValue() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( DecimalMinMaxValidatorBoundaryTest.class )
				.field( "d" )
				.constraint( new DecimalMaxDef().value( "0.1" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		this.d = 0.1;

		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNoViolations( constraintViolations );
	}


	@Test
	@TestForIssue(jiraKey = "HV-508")
	public void testDoubleTrouble() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( DecimalMinMaxValidatorBoundaryTest.class )
				.field( "d" )
				.constraint( new DecimalMaxDef().value( "1.2" ) );
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();

		this.d = 1.0;
		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNoViolations( constraintViolations );

		this.d = 1.1;
		constraintViolations = validator.validate( this );
		assertNoViolations( constraintViolations );

		this.d = 1.19;
		constraintViolations = validator.validate( this );
		assertNoViolations( constraintViolations );

		this.d = 1.20;
		constraintViolations = validator.validate( this );
		assertNoViolations( constraintViolations );

		this.d = 1.3;
		constraintViolations = validator.validate( this );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DecimalMax.class )
		);

		this.d = 1.51;
		constraintViolations = validator.validate( this );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DecimalMax.class )
		);

		this.d = 1.9;
		constraintViolations = validator.validate( this );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DecimalMax.class )
		);

		this.d = 2.000000001;
		constraintViolations = validator.validate( this );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DecimalMax.class )
		);
	}
}
