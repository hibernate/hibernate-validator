/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

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

		assertCorrectPropertyPaths( violations, "GreetingService.<return value>.hello" );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
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
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
		assertCorrectPropertyPaths( violations, "GreetingService.arg0.name" );
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
				.property( "name", ElementType.FIELD )
				.constraint( new NotNullDef().message( "name must not be null" ).groups( TestGroup.class ) );
		config.addMapping( mapping );


		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( User.class );
		Object[] parameterValues = new Object[] { new User( null ) };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);
		assertCorrectConstraintViolationMessages( violations, "name must not be null" );
		assertCorrectPropertyPaths( violations, "GreetingService.arg0.name" );
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
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
		assertCorrectPropertyPaths( violations, "GreetingService.arg0" );
	}

	@Test
	public void testGenericParameterConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class )
				.parameter( 0 )
				.constraint( new GenericConstraintDef<Size>( Size.class ).param( "min", 1 ).param( "max", 10 ) );
		config.addMapping( mapping );

		Constructor<GreetingService> constructor = GreetingService.class.getConstructor( String.class );
		Object[] parameterValues = new Object[] { "" };

		ExecutableValidator executableValidator = getConfiguredExecutableValidator();

		Set<ConstraintViolation<GreetingService>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);

		assertCorrectConstraintViolationMessages( violations, "size must be between 1 and 10" );
		assertCorrectPropertyPaths( violations, "GreetingService.arg0" );
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

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 1 and 10",
				"size must be between 2 and 10"
		);
		assertCorrectPropertyPaths( violations, "GreetingService.arg0", "GreetingService.arg0" );
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

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 1 and 10",
				"size must be between 1 and 10"
		);
		assertCorrectPropertyPaths( violations, "GreetingService.arg0", "GreetingService.arg1" );
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

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 1 and 10",
				"size must be between 2 and 10"
		);
		assertCorrectPropertyPaths( violations, "GreetingService.arg0", "GreetingService.arg0" );
	}

	@Test
	public void testReturnValueConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<ValidGreetingService>( ValidGreetingService.class ).message(
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

		assertCorrectConstraintViolationMessages( violations, "invalid" );
		assertCorrectPropertyPaths( violations, "GreetingService.<return value>" );
	}

	@Test
	public void testMultipleReturnValueConstraints() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<ValidGreetingService>( ValidGreetingService.class ).message(
								"invalid 1"
						)
				)
				.constraint(
						new GenericConstraintDef<ValidGreetingService>( ValidGreetingService.class ).message(
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

		assertCorrectConstraintViolationMessages( violations, "invalid 1", "invalid 2" );
		assertCorrectPropertyPaths( violations, "GreetingService.<return value>", "GreetingService.<return value>" );
	}

	@Test
	public void testProgrammaticAndAnnotationReturnValueConstraintsAddUp() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( CharSequence.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<ValidGreetingService>( ValidGreetingService.class ).message(
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

		assertCorrectConstraintViolationMessages( violations, "invalid 1", "invalid 2" );
		assertCorrectPropertyPaths( violations, "GreetingService.<return value>", "GreetingService.<return value>" );
	}

	@Test
	public void shouldDetermineConstraintTargetForReturnValueConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class, String.class )
				.returnValue()
				.constraint(
						new GenericConstraintDef<GenericAndCrossParameterConstraint>(
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

		assertCorrectConstraintViolationMessages( violations, "default message" );
		assertCorrectPropertyPaths( violations, "GreetingService.<return value>" );
	}

	@Test
	public void crossParameterConstraint() throws Exception {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( GreetingService.class )
				.constructor( String.class, String.class )
				.crossParameter()
				.constraint(
						new GenericConstraintDef<GenericAndCrossParameterConstraint>(
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

		assertCorrectConstraintViolationMessages( violations, "default message" );
		assertCorrectPropertyPaths( violations, "GreetingService.<cross-parameter>" );
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

		public class Validator implements ConstraintValidator<ValidGreetingService, GreetingService> {

			@Override
			public void initialize(ValidGreetingService constraintAnnotation) {
				//nothing to do
			}

			@Override
			public boolean isValid(GreetingService value, ConstraintValidatorContext context) {
				return false;
			}
		}
	}
}
