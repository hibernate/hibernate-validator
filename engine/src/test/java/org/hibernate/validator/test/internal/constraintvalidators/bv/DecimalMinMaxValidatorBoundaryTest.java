/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DecimalMaxDef;
import org.hibernate.validator.cfg.defs.DecimalMinDef;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.DecimalMin;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

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
				.property( "d", FIELD )
				.constraint( new DecimalMinDef().value( "0.100000000000000005" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		this.d = 0.1;

		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, DecimalMin.class );
	}

	@Test
	public void testDecimalMaxValue() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( DecimalMinMaxValidatorBoundaryTest.class )
				.property( "d", FIELD )
				.constraint( new DecimalMaxDef().value( "0.1" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		this.d = 0.1;

		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 0 );
	}


	@Test
	@TestForIssue(jiraKey = "HV-508")
	public void testDoubleTrouble() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( DecimalMinMaxValidatorBoundaryTest.class )
				.property( "d", FIELD )
				.constraint( new DecimalMaxDef().value( "1.2" ) );
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();

		this.d = 1.0;
		Set<ConstraintViolation<DecimalMinMaxValidatorBoundaryTest>> constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 0 );

		this.d = 1.1;
		constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 0 );

		this.d = 1.19;
		constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 0 );

		this.d = 1.20;
		constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 0 );

		this.d = 1.3;
		constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );

		this.d = 1.51;
		constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );

		this.d = 1.9;
		constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );

		this.d = 2.000000001;
		constraintViolations = validator.validate( this );
		assertNumberOfViolations( constraintViolations, 1 );
	}
}
