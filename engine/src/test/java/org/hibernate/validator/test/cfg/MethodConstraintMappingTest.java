/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.Set;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the definition of method constraints with the programmatic API.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
					.withPropertyPath( pathWith()
							.method( "greet" )
							.returnValue()
							.property( "message" )
					)
			);
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
				.field( "message" )
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "message must not be null" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
									.property( "message" )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "user", 0 )
									.property( "name" )
							)
			);
		}
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000135.*"
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 5 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "string", 0 )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "user", 0 )
									.property( "name" )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "user", 0 )
							)
			);
		}
	}

	@Test
	public void testGenericParameterConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.parameter( 0 )
				.constraint( new GenericConstraintDef<>( Size.class ).param( "min", 1 ).param( "max", 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "string", 0 )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "string", 0 )
							),
					violationOf( Size.class )
							.withMessage( "size must be between 2 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "string", 0 )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "string1", 0 )
							),
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "string2", 1 )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "sayHello" )
									.parameter( "name", 0 )
							),
					violationOf( Size.class )
							.withMessage( "size must be between 2 and 10" )
							.withPropertyPath( pathWith()
									.method( "sayHello" )
									.parameter( "name", 0 )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "user", 0 )
							)
			);
		}

		try {
			service.greet( new User( null ) );

			fail( "Expected exception wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.parameter( "user", 0 )
									.property( "name" )
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							),
					violationOf( Size.class )
							.withMessage( "size must be between 2 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							)
			);
		}
	}

	@Test
	public void testGenericReturnValueConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class )
				.returnValue()
				.constraint( new GenericConstraintDef<>( Size.class ).param( "min", 1 ).param( "max", 10 ) );
		config.addMapping( mapping );

		try {
			GreetingService service = getValidatingProxy(
					wrappedObject,
					config.buildValidatorFactory().getValidator()
			);
			service.greet( "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							)
			);
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							),
					violationOf( Size.class )
							.withMessage( "size must be between 2 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							)
			);
		}
	}

	@Test
	public void constraintConfiguredOnPropertyIsEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.getter( "hello" )
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withPropertyPath( pathWith()
									.method( "getHello" )
									.returnValue()
							)
			);
		}
	}

	@Test
	public void cascadeConfiguredOnPropertyIsEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.getter( "user" )
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withPropertyPath( pathWith()
									.method( "getUser" )
									.returnValue()
									.property( "name" )
							)
			);
		}
	}

	@Test
	public void constraintConfiguredOnFieldIsNotEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingServiceImpl.class )
				.field( "hello" )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );

		GreetingService service = getValidatingProxy( wrappedObject, config.buildValidatorFactory().getValidator() );
		assertNull( service.getHello() );
	}

	@Test
	public void cascadeConfiguredOnFieldIsNotEvaluatedByMethodValidation() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingServiceImpl.class )
				.field( "user" )
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
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withPropertyPath( pathWith()
								.property( "hello" )
						)
		);
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

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withPropertyPath( pathWith()
								.property( "user" )
								.property( "name" )
						)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-769")
	public void shouldDetermineConstraintTargetForReturnValueConstraint() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "greet", String.class, String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<>(
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( GenericAndCrossParameterConstraint.class )
							.withMessage( "default message" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							),
					violationOf( Size.class )
							.withMessage( "size must be between 1 and 10" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.returnValue()
							)
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
						new GenericConstraintDef<>(
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
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( GenericAndCrossParameterConstraint.class )
							.withMessage( "default message" )
							.withPropertyPath( pathWith()
									.method( "greet" )
									.crossParameter()
							)
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1220")
	public void crossParameterConstraintOnMethodReturningVoid() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.method( "sayNothing", String.class )
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
			service.sayNothing( "" );

			fail( "Expected exception wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( GenericAndCrossParameterConstraint.class )
							.withMessage( "default message" )
							.withPropertyPath( pathWith()
									.method( "sayNothing" )
									.crossParameter()
							)
			);
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

		void sayNothing(String string1);

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

		@Override
		public void sayNothing(String string1) {
			// Nothing to do
		}
	}
}
