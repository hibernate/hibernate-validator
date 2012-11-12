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

package org.hibernate.validator.test.internal.engine.messageinterpolation;

import java.util.Date;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DecimalMinDef;
import org.hibernate.validator.cfg.defs.FutureDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.NullDef;
import org.hibernate.validator.messageinterpolation.ValueFormatterMessageInterpolator;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

/**
 * Test for {@link org.hibernate.validator.messageinterpolation.ValueFormatterMessageInterpolator}.
 *
 * @author Hardy Ferentschik
 */
public class ValueFormatterMessageInterpolatorTest {
	private HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
		config.messageInterpolator( new ValueFormatterMessageInterpolator() );
	}

	@Test
	public void testSimpleValidatedValueInterpolation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "date", FIELD )
				.constraint( new FutureDef().message( "${validatedValue}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Date past = new Date( System.currentTimeMillis() - 60000 ); // current date minus one minute
		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( past ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, past.toString() );
	}

	@Test
	public void testMultipleValidatedValuesInMessageTemplate() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "date", FIELD )
				.constraint( new FutureDef().message( "${validatedValue} ${validatedValue}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Date past = new Date( System.currentTimeMillis() - 60000 ); // current date minus one minute
		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( past ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, past.toString() + " " + past.toString() );
	}

	@Test
	public void testEscapedCurlyBraces() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "date", FIELD )
				.constraint( new FutureDef().message( "\\{${validatedValue}\\}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Date past = new Date( System.currentTimeMillis() - 60000 ); // current date minus one minute
		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( past ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "{" + past.toString() + "}" );
	}

	@Test
	public void testFormattedDate() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "date", FIELD )
				.constraint( new FutureDef().message( "${validatedValue:%1$ty}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Date past = new Date( System.currentTimeMillis() - 60000 ); // current date minus one minute
		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( past ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, String.format( "%1$ty", past ) );
	}

	@Test
	public void testNullValueValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "date", FIELD )
				.constraint( new NotNullDef().message( "${validatedValue}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( (Date) null ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "null" );
	}

	@Test
	public void testFormattedDouble() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "doubleValue", FIELD )
				.constraint( new DecimalMinDef().value( "1.0" ).message( "${validatedValue: '%1$5f' }" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( 0.1 ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, " '0.100000' " );
	}

	@Test
	public void testCurlyBraceInFormat() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "doubleValue", FIELD )
				.constraint( new DecimalMinDef().value( "1.0" ).message( "${validatedValue: {%1$5f} }" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( 0.1 ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, " {0.100000} " );
	}

	@Test
	public void testColonInFormat() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "doubleValue", FIELD )
				.constraint( new DecimalMinDef().value( "1.0" ).message( "${validatedValue::%1$5f:}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( 0.1 ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, ":0.100000:" );
	}

	@Test
	public void testValidatedValueAndAdditionalUnknownParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "doubleValue", FIELD )
				.constraint(
						new DecimalMinDef().value( "1.0" ).message( "${validatedValue: '%1$5f' } ${foo}" )
				);

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( 0.1 ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, " '0.100000'  ${foo}" );
	}

	@Test
	public void testNoClosingBrace() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "doubleValue", FIELD )
				.constraint( new DecimalMinDef().value( "1.0" ).message( "${validatedValue{" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( 0.1 ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "${validatedValue{" );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Missing format string in template:.*"
	)
	public void testMissingFormatString() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "doubleValue", FIELD )
				.constraint( new DecimalMinDef().value( "1.0" ).message( "${validatedValue:}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new TestClass( 0.1 ) );
	}

	@Test
	public void testFormattable() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "greeter", FIELD )
				.constraint( new NullDef().message( "${validatedValue: '%1$s' }" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<TestClass>> violations = validator.validate( new TestClass( new Greeter() ) );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, " 'Hello world' " );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Invalid format:.*"
	)
	public void testIllegalFormat() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( TestClass.class )
				.property( "doubleValue", FIELD )
						// z is an unknown format
				.constraint( new DecimalMinDef().value( "1.0" ).message( "${validatedValue:%1$z}" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new TestClass( 0.1 ) );
	}

	@SuppressWarnings("unused")
	class TestClass {
		private Date date;
		private Double doubleValue;
		private Greeter greeter;

		TestClass(Date date) {
			this.date = date;
		}

		TestClass(Double doubleValue) {
			this.doubleValue = doubleValue;
		}

		TestClass(Greeter greeter) {
			this.greeter = greeter;
		}
	}

	class Greeter implements Formattable {
		public void formatTo(Formatter formatter, int flags, int width, int precision) {
			formatter.format( "Hello world" );
		}
	}
}
