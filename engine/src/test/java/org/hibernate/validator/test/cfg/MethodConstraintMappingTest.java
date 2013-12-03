/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.annotation.ElementType;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * Tests the definition of method constraints with the programmatic API.
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class MethodConstraintMappingTest {
	private HibernateValidatorConfiguration config;
	private GreetingService wrappedObject;

	@BeforeClass
	public void setUp() {
		wrappedObject = new GreetingServiceImpl();
	}

	@BeforeMethod
	public void setUpTest() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	public void testCascadingMethodReturnDefinition() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", User.class )
				.returnValue()
				.valid();
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );

		try {
			service.greet( new User( "foo" ) );
			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertCorrectConstraintViolationMessages( e, "may not be null" );
			assertCorrectPropertyPaths( e, "greet.<return value>.message" );
		}
	}

	@Test
	public void testCascadingMethodReturnDefinitionWithGroupConversion() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", User.class )
				.returnValue()
				.valid()
				.convertGroup( Default.class ).to( TestGroup.class )
				.type( Message.class )
				.property( "message", ElementType.FIELD )
				.constraint(
						new NotNullDef()
								.message( "message must not be null" )
								.groups( TestGroup.class )
				);

		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );

		try {
			service.greet( new User( "foo" ) );
			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertCorrectConstraintViolationMessages( e, "message must not be null" );
			assertCorrectPropertyPaths( e, "greet.<return value>.message" );
		}
	}

	@Test
	public void testCascadingMethodParameterDefinition() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", User.class )
				.parameter( 0 )
				.valid();
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );

		try {
			service.greet( new User( null ) );
			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertCorrectConstraintViolationMessages( e, "may not be null" );
			assertCorrectPropertyPaths( e, "greet.arg0.name" );
		}
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Type .*GreetingService doesn't have a method greet().*"
	)
	public void testCascadingDefinitionOnMissingMethod() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet" )
				.returnValue()
				.valid();

		config.buildValidatorFactory().getValidator();
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "HV000056.*"
	)
	public void testCascadingDefinitionOnInvalidMethodParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", User.class )
				.parameter( 1 )
				.valid();

		config.buildValidatorFactory().getValidator();
	}

	@Test
	public void testOverridingMethodMayDefineSameConstraintsAsOverriddenMethod() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 5 ).max( 10 ) )
				.type( GreetingServiceImpl.class )
				.method( "greet", String.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 5 ).max( 10 ) );
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );

		try {
			service.greet( "Hi" );
		}
		catch ( ConstraintViolationException e ) {
			assertCorrectConstraintViolationMessages( e, "size must be between 5 and 10" );
			assertCorrectPropertyPaths( e, "greet.arg0" );
		}
	}

	@Test
	public void testParameterCanMarkedAsCascadedSeveralTimesInTheHierarchy() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", User.class )
				.parameter( 0 )
				.valid()
				.type( GreetingServiceImpl.class )
				.method( "greet", User.class )
				.parameter( 0 )
				.valid();
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );

		try {
			service.greet( new User( null ) );
		}
		catch ( ConstraintViolationException e ) {
			assertCorrectConstraintViolationMessages( e, "may not be null" );
			assertCorrectPropertyPaths( e, "greet.arg0.name" );
		}
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	public void testCascadingMethodParameterDefinedOnlyOnSubType() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingServiceImpl.class )
				.method( "greet", User.class )
				.parameter( 0 )
				.valid();
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );

		service.greet( new User( null ) );
	}

	@Test
	public void testParameterConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", User.class )
				.parameter( 0 )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( (User) null );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages( e, "may not be null" );
			assertCorrectPropertyPaths( e, "greet.arg0" );
		}
	}

	@Test
	public void testGenericParameterConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.parameter( 0 )
				.constraint( new GenericConstraintDef<Size>( Size.class ).param( "min", 1 ).param( "max", 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "size must be between 1 and 10"
			);
			assertCorrectPropertyPaths( e, "greet.arg0" );
		}
	}

	@Test
	public void testMultipleParameterConstraintsAtSameParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 1 ).max( 10 ) )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "size must be between 1 and 10", "size must be between 2 and 10"
			);
			assertCorrectPropertyPaths( e, "greet.arg0", "greet.arg0" );
		}
	}

	@Test
	public void testMultipleParameterConstraintsAtDifferentParameters() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class, String.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 1 ).max( 10 ) )
				.parameter( 1 )
				.constraint( new SizeDef().min( 1 ).max( 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "", "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "size must be between 1 and 10", "size must be between 1 and 10"
			);
			assertCorrectPropertyPaths( e, "greet.arg0", "greet.arg1" );
		}
	}

	@Test
	public void testProgrammaticAndAnnotationParameterConstraintsAddUp() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "sayHello", String.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.sayHello( "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "size must be between 1 and 10", "size must be between 2 and 10"
			);
			assertCorrectPropertyPaths( e, "sayHello.arg0", "sayHello.arg0" );
		}
	}

	@Test
	public void testConstraintAtCascadedParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", User.class )
				.parameter( 0 )
				.constraint( new NotNullDef() )
				.valid();
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );

		try {
			service.greet( (User) null );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages( e, "may not be null" );
			assertCorrectPropertyPaths( e, "greet.arg0" );
		}

		try {
			service.greet( new User( null ) );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages( e, "may not be null" );
			assertCorrectPropertyPaths( e, "greet.arg0.name" );
		}
	}

	@Test
	public void testReturnValueConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.returnValue()
				.constraint( new SizeDef().min( 1 ).max( 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "Hello" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages( e, "size must be between 1 and 10" );
			assertCorrectPropertyPaths( e, "greet.<return value>" );
		}
	}

	@Test
	public void testMultipleReturnValueConstraints() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.returnValue()
				.constraint( new SizeDef().min( 1 ).max( 10 ) )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "Hello" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "size must be between 1 and 10", "size must be between 2 and 10"
			);
			assertCorrectPropertyPaths( e, "greet.<return value>", "greet.<return value>" );
		}
	}

	@Test
	public void testGenericReturnValueConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.returnValue()
				.constraint( new GenericConstraintDef<Size>( Size.class ).param( "min", 1 ).param( "max", 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "size must be between 1 and 10"
			);
			assertCorrectPropertyPaths( e, "greet.<return value>" );
		}
	}

	@Test
	public void testProgrammaticAndAnnotationReturnValueConstraintsAddUp() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class, String.class )
				.returnValue()
				.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "Hello", "World" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "size must be between 1 and 10", "size must be between 2 and 10"
			);
			assertCorrectPropertyPaths( e, "greet.<return value>", "greet.<return value>" );
		}
	}

	@Test
	public void constraintConfiguredOnPropertyIsEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.property( "hello", ElementType.METHOD )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.getHello();

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "may not be null"
			);
			assertCorrectPropertyPaths( e, "getHello.<return value>" );
		}
	}

	@Test
	public void cascadeConfiguredOnPropertyIsEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.property( "user", ElementType.METHOD )
				.valid();
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.getUser();

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "may not be null"
			);
			assertCorrectPropertyPaths( e, "getUser.<return value>.name" );
		}
	}

	@Test
	public void constraintConfiguredOnFieldIsNotEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingServiceImpl.class )
				.property( "hello", ElementType.FIELD )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );
		assertNull( service.getHello() );
	}

	@Test
	public void cascadeConfiguredOnFieldIsNotEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingServiceImpl.class )
				.property( "user", ElementType.FIELD )
				.valid();
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );
		assertNull( service.getUser().getName() );
	}

	@Test
	public void constraintConfiguredOnMethodIsEvaluatedByPropertyValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "getHello" )
				.returnValue()
				.constraint( new NotNullDef() );
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<GreetingServiceImpl>> violations = validator.validateProperty(
				new GreetingServiceImpl(), "hello"
		);

		assertCorrectConstraintViolationMessages( violations, "may not be null" );
		assertCorrectPropertyPaths( violations, "hello" );
	}

	@Test
	public void cascadeConfiguredOnMethodIsEvaluatedByPropertyValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "getUser" )
				.returnValue()
				.valid();
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<GreetingServiceImpl>> violations = validator.validate( new GreetingServiceImpl() );

		assertCorrectConstraintViolationMessages( violations, "may not be null" );
		assertCorrectPropertyPaths( violations, "user.name" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-769")
	public void shouldDetermineConstraintTargetForReturnValueConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class, String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<GenericAndCrossParameterConstraint>(
								GenericAndCrossParameterConstraint.class
						)
				);
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		GreetingService service = getValidatingProxy( wrappedObject, validator );

		try {
			service.greet( null, null );
			fail( "Expected exception wasn't thrown" );
		}
		catch ( ConstraintViolationException cve ) {
			assertThat( cve.getConstraintViolations() ).containsOnlyPaths(
					pathWith().method( "greet" ).returnValue()
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-642")
	public void crossParameterConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class, String.class )
				.crossParameter()
				.constraint(
						new GenericConstraintDef<GenericAndCrossParameterConstraint>(
								GenericAndCrossParameterConstraint.class
						)
				);
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "", "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e, "default message"
			);
			assertCorrectPropertyPaths( e, "greet.<cross-parameter>" );
		}
	}

	private interface TestGroup {
	}

	public class User {

		@NotNull
		private final String name;

		public User(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public class Message {

		@NotNull
		private final String message;

		public Message(String message) {
			this.message = message;
		}
	}

	public interface GreetingService {

		Message greet(User user);

		String greet(String string);

		@Size(min = 1, max = 10)
		String greet(String string1, String string2);

		Message sayHello(@Size(min = 1, max = 10) String name);

		Message getHello();

		User getUser();

	}

	public class GreetingServiceImpl implements GreetingService {

		@SuppressWarnings("unused")
		private Message hello;

		@SuppressWarnings("unused")
		private User user;

		@Override
		public Message greet(User user) {
			return new Message( null );
		}

		@Override
		public String greet(String string) {
			return "";
		}

		@Override
		public String greet(String string1, String string2) {
			return "";
		}

		@Override
		public Message sayHello(String name) {
			return null;
		}

		@Override
		public Message getHello() {
			return null;
		}

		@Override
		public User getUser() {
			return new User( null );
		}
	}
}
