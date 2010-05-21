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
import org.hibernate.validator.cfg.AssertTrueDefinition;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.FutureDefinition;
import org.hibernate.validator.cfg.MinDefinition;
import org.hibernate.validator.cfg.NotNullDefinition;
import org.hibernate.validator.test.util.TestUtil;
import org.hibernate.validator.util.LoggerFactory;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.test.util.TestUtil.assertConstraintViolation;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintMappingTest {
	private static final Logger log = LoggerFactory.make();

	@Test
	public void testConstraintMapping() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDefinition.class )
				.property( "numberOfRunners", FIELD )
				.constraint( MinDefinition.class ).value( 1 );

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
				.constraint( NotNullDefinition.class );

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
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDefinition.class )
				.type( Tournament.class )
				.property( "tournamentDate", METHOD )
				.constraint( FutureDefinition.class );

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
				.constraint( AssertTrueDefinition.class );

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
			mapping.type( Marathon.class )
					.property( "numberOfRunners", METHOD )
					.constraint( NotNullDefinition.class );
			fail();
		}
		catch ( ValidationException e ) {
			log.debug( e.toString() );
		}
	}
}


