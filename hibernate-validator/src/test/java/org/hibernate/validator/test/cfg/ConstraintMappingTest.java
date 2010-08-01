// $Id$
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
package org.hibernate.validator.test.cfg;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.FutureDef;
import org.hibernate.validator.cfg.defs.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotEmptyDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.RangeDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.test.util.TestUtil;
import org.hibernate.validator.util.LoggerFactory;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.test.util.TestUtil.assertConstraintViolation;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintMappingTest {
	private static final Logger log = LoggerFactory.make();

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullConstraintMapping() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( ( ConstraintMapping ) null );
		config.buildValidatorFactory();
	}

	@Test
	public void testConstraintMapping() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class )
				.property( "numberOfHelpers", FIELD )
				.constraint( MinDef.class ).value( 1 );

		assertTrue( mapping.getConstraintConfig().containsKey( Marathon.class ) );
		assertTrue( mapping.getConstraintConfig().get( Marathon.class ).size() == 2 );
	}

	@Test
	public void testNoConstraintViolationForUnmappedEntity() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( new Marathon() );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	public void testSingleConstraint() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class );

		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( new Marathon() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );
	}

	@Test
	public void testInheritedConstraint() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );

		ConstraintMapping mapping = new ConstraintMapping();
		mapping
				.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class )
				.type( Tournament.class )
				.property( "tournamentDate", METHOD )
				.constraint( FutureDef.class );

		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );
		Calendar cal = GregorianCalendar.getInstance();
		cal.set( Calendar.YEAR, -1 );
		marathon.setTournamentDate( cal.getTime() );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be in the future" );
	}

	@Test
	public void testValid() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.valid( "runners", METHOD )
				.type( Runner.class )
				.property( "paidEntryFee", FIELD )
				.constraint( AssertTrueDef.class );

		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 0 );

		marathon.addRunner( new Runner() );
		violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be true" );
	}

	@Test
	public void testSingleConstraintWrongAccessType() {
		ConstraintMapping mapping = new ConstraintMapping();
		try {
			mapping
					.type( Marathon.class )
					.property( "numberOfHelpers", METHOD )
					.constraint( NotNullDef.class );
			fail();
		}
		catch ( ValidationException e ) {
			log.debug( e.toString() );
		}
	}

	@Test
	public void testDefaultGroupSequence() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );

		ConstraintMapping mapping = new ConstraintMapping();
		mapping
				.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class ).groups( Foo.class )
				.property( "runners", METHOD )
				.constraint( NotEmptyDef.class );

		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Marathon marathon = new Marathon();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );

		marathon.setName( "Stockholm Marathon" );
		violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be empty" );
	}

	@Test
	public void testMultipleConstraintOfTheSameType() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( SizeDef.class ).min( 5 )
				.constraint( SizeDef.class ).min( 10 );

		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "Foo" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 2 );

		marathon.setName( "Foobar" );
		violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );

		marathon.setName( "Stockholm Marathon" );
		violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	public void testCustomConstraintTypeMissingParameter() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.constraint( GenericConstraintDef.class )
				.constraintType( MarathonConstraint.class );

		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( mapping );
		try {
			config.buildValidatorFactory();
			fail( "MarathonConstraints needs a parameter" );
		}
		catch ( ValidationException e ) {
			assertTrue( e.getMessage().contains( "No value provided for minRunner" ) );
		}
	}

	@Test
	public void testCustomConstraintType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.constraint( GenericConstraintDef.class )
				.constraintType( MarathonConstraint.class )
				.param( "minRunner", 100 )
				.message( "Needs more runners" );

		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "Stockholm Marathon" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "Needs more runners" );

		for ( int i = 0; i < 100; i++ ) {
			marathon.addRunner( new Runner() );
		}
		violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 0 );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testNullBean() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( null )
				.constraint( GenericConstraintDef.class )
				.constraintType( MarathonConstraint.class );

		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( mapping );
		config.buildValidatorFactory();
	}

	/**
	 * HV-355 (parameter names of RangeDef wrong)
	 */
	@Test
	public void testRangeDef() {

		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Runner.class )
				.property( "age", METHOD )
				.constraint( RangeDef.class )
				.min( 12 )
				.max( 99 );


		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<Runner>> violations = validator.validate( new Runner() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be between 12 and 99" );
	}

	public interface Foo {
	}
}


