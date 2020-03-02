/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.groups.Default;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the definition of constructor constraints with the programmatic API.
 *
 * @author Gunnar Morling
 */
public class ConstructorConstraintMappingTest {
	private HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUpTest() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	public void testCascadingConstructorReturnDefinition() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor()
				.returnValue()
				.valid();
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor();
		GreetingService createdObject = new GreetingService();

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				createdObject
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.returnValue()
								.property( "hello" )
						)
		);
	}

	@Test
	public void testCascadingConstructorParameterDefinition() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( User.class )
				.parameter( 0 )
				.valid();
		config.addMapping( mapping );


		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( User.class );
		Object[] parameterValues = new Object[] { new User( null ) };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "user", 0 )
								.property( "name" )
						)
		);
	}

	@Test
	public void testCascadingConstructorParameterDefinitionWithGroupConversion() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( User.class )
				.parameter( 0 )
				.valid()
				.convertGroup( Default.class ).to( TestGroup.class )
				.type( User.class )
				.field( "name" )
				.constraint( new NotNullDef().message( "name must not be null" ).groups( TestGroup.class ) );
		config.addMapping( mapping );


		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( User.class );
		Object[] parameterValues = new Object[] { new User( null ) };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "name must not be null" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "user", 0 )
								.property( "name" )
						)
		);
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000133.*"
	)
	public void testCascadingDefinitionOnMissingMethod() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( Date.class );
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "HV000056.*"
	)
	public void testCascadingDefinitionOnInvalidMethodParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( User.class )
				.parameter( 1 );
	}

	@Test
	public void testParameterConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( User.class )
				.parameter( 0 )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( User.class );
		Object[] parameterValues = new Object[] { null };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "user", 0 )
						)
		);
	}

	@Test
	public void testGenericParameterConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class )
				.parameter( 0 )
				.constraint( new GenericConstraintDef<>( Size.class ).param( "min", 1 ).param( "max", 10 ) );
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class );
		Object[] parameterValues = new Object[] { "" };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 1 and 10" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "message", 0 )
						)
		);
	}

	@Test
	public void testMultipleParameterConstraintsAtSameParameter() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 1 ).max( 10 ) )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class );
		Object[] parameterValues = new Object[] { "" };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 1 and 10" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "message", 0 )
						),
				violationOf( Size.class )
						.withMessage( "size must be between 2 and 10" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "message", 0 )
						)
		);
	}

	@Test
	public void testMultipleParameterConstraintsAtDifferentParameters() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class, String.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 1 ).max( 10 ) )
				.parameter( 1 )
				.constraint( new SizeDef().min( 1 ).max( 10 ) );
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class, String.class );
		Object[] parameterValues = new Object[] { "", "" };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 1 and 10" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "message", 0 )
						),
				violationOf( Size.class )
						.withMessage( "size must be between 1 and 10" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "anotherMessage", 1 )
						)
		);
	}

	@Test
	public void testProgrammaticAndAnnotationParameterConstraintsAddUp() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( CharSequence.class )
				.parameter( 0 )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( CharSequence.class );
		Object[] parameterValues = new Object[] { "" };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 1 and 10" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "message", 0 )
						),
				violationOf( Size.class )
						.withMessage( "size must be between 2 and 10" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.parameter( "message", 0 )
						)
		);
	}

	@Test
	public void testReturnValueConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<>( ValidGreetingService.class ).message(
								"invalid"
						)
				);
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class );

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				new GreetingService( "" )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidGreetingService.class )
						.withMessage( "invalid" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.returnValue()
						)
		);
	}

	@Test
	public void testMultipleReturnValueConstraints() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<>( ValidGreetingService.class ).message(
								"invalid 1"
						)
				)
				.constraint(
						new GenericConstraintDef<>( ValidGreetingService.class ).message(
								"invalid 2"
						)
				);
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class );

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				new GreetingService( "" )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidGreetingService.class )
						.withMessage( "invalid 1" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.returnValue()
						),
				violationOf( ValidGreetingService.class )
						.withMessage( "invalid 2" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.returnValue()
						)
		);

	}

	@Test
	public void testProgrammaticAndAnnotationReturnValueConstraintsAddUp() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( CharSequence.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<>( ValidGreetingService.class ).message(
								"invalid 2"
						)
				);
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( CharSequence.class );

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				new GreetingService( "" )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidGreetingService.class )
						.withMessage( "invalid 1" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.returnValue()
						),
				violationOf( ValidGreetingService.class )
						.withMessage( "invalid 2" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.returnValue()
						)
		);
	}

	@Test
	public void shouldDetermineConstraintTargetForReturnValueConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class, String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<>(
								GenericAndCrossParameterConstraint.class
						)
				);
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class, String.class );

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				new GreetingService( "", "" )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( GenericAndCrossParameterConstraint.class )
						.withMessage( "default message" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.returnValue()
						)
		);
	}

	@Test
	public void crossParameterConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class, String.class )
				.crossParameter()
				.constraint(
						new GenericConstraintDef<>(
								GenericAndCrossParameterConstraint.class
						)
				);
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class, String.class );
		Object[] parameterValues = new Object[] { "", "" };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( GenericAndCrossParameterConstraint.class )
						.withMessage( "default message" )
						.withPropertyPath( pathWith()
								.constructor( GreetingService.class )
								.crossParameter()
						)
		);
	}

	private ExecutableValidator getConfiguredExecutableValidator() {
		return config.buildValidatorFactory().getValidator().forExecutables();
	}

	public interface TestGroup {
	}

	public static class User {

		@NotNull
		private final String name;

		public User(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static class Message {

		@NotNull
		private final String message;

		public Message(String message) {
			this.message = message;
		}
	}

	public static class GreetingService {

		@NotNull
		private Message hello;

		public GreetingService() {
		}

		public GreetingService(User user) {
		}

		public GreetingService(String message) {
		}

		public GreetingService(String message, String anotherMessage) {
		}

		@ValidGreetingService(message = "invalid 1")
		public GreetingService(@Size(min = 1, max = 10) CharSequence message) {
		}
	}

	@Target({ CONSTRUCTOR, ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = ValidGreetingService.Validator.class)
	@Documented
	public @interface ValidGreetingService {
		String message() default "{ValidGreetingService.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		class Validator implements ConstraintValidator<ValidGreetingService, GreetingService> {

			@Override
			public boolean isValid(GreetingService value, ConstraintValidatorContext context) {
				return false;
			}
		}
	}
}
