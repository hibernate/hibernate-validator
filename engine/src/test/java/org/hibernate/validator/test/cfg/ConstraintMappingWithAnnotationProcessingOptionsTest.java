/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.MethodType;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NullDef;
import org.hibernate.validator.test.constraints.Object;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link org.hibernate.validator.cfg.ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
@Test
public class ConstraintMappingWithAnnotationProcessingOptionsTest {
	HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	public void testIgnoreAllAnnotationsOnType() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping
			.type( Foo.class )
				.ignoreAllAnnotations()
			.type( Doer.class )
				.ignoreAllAnnotations();

		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		assertFalse( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );

		assertEquals( validator.getConstraintsForClass( Doer.class ).getConstrainedConstructors().size(), 0 );
		assertEquals( validator.getConstraintsForClass( Doer.class ).getConstrainedMethods( MethodType.NON_GETTER ).size(), 0 );
	}

	@Test
	public void testIgnoreClassConstraints() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Fu.class ).ignoreAnnotations( true );
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		assertFalse( validator.getConstraintsForClass( Fu.class ).isBeanConstrained() );
	}

	@Test
	public void testIgnoreAnnotationsOnProperty() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "property" )
				.ignoreAnnotations( true );
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		assertFalse( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );
	}

	@Test
	public void testIgnoreAnnotationsRespectsFieldVsGetterAccess() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.getter( "property" )
				.ignoreAnnotations( true );
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		assertTrue( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );
	}

	@Test
	public void testConvertNotNullToNull() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "property" )
		);

		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bar.class )
				.field( "property" )
				.ignoreAnnotations( true )
				.constraint( new NullDef() );
		config.addMapping( mapping );
		validator = config.buildValidatorFactory().getValidator();
		violations = validator.validate( new Bar() );

		assertNoViolations( violations );
	}

	@Test
	public void testIgnoreAnnotationsOnMethod() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.method( "doSomething", String.class )
				.ignoreAnnotations( true );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doSomething", String.class );

		assertNull( descriptor );
	}

	@Test
	public void testIgnoreAnnotationsOnTypeAndMethod() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.ignoreAnnotations( true )
				.method( "doSomething", String.class )
					.ignoreAnnotations( false );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doSomething", String.class );

		assertTrue( descriptor.hasConstrainedParameters() );
		assertTrue( descriptor.hasConstrainedReturnValue() );
	}

	@Test
	public void testIgnoreAnnotationsOnMethodParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.method( "doSomething", String.class )
					.parameter( 0 )
					.ignoreAnnotations( true );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doSomething", String.class );

		assertFalse( descriptor.hasConstrainedParameters() );
	}

	@Test
	public void testIgnoreAnnotationsOnMethodAndParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();

		mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.method( "doSomething", String.class )
					.ignoreAnnotations( true )
					.parameter( 0 )
						.ignoreAnnotations( false );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doSomething", String.class );

		assertTrue( descriptor.hasConstrainedParameters(), "Setting given for parameter should take precedence" );
	}

	@Test
	public void testIgnoreAnnotationsOnCrossParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.method( "doAnotherThing", String.class )
					.crossParameter()
					.ignoreAnnotations( true );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doAnotherThing", String.class );

		assertNull( descriptor );
	}

	@Test
	public void testIgnoreAnnotationsOnMethodAndCrossParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();

		mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.method( "doAnotherThing", String.class )
					.ignoreAnnotations( true )
					.crossParameter()
						.ignoreAnnotations( false );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doAnotherThing", String.class );

		assertTrue( descriptor.hasConstrainedParameters(), "Setting given for cross-parameter should take precedence" );
	}

	@Test
	public void testIgnoreAnnotationsOnMethodReturnValue() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.method( "doSomething", String.class )
					.returnValue()
						.ignoreAnnotations( true );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doSomething", String.class );

		assertFalse( descriptor.hasConstrainedReturnValue() );
	}

	@Test
	public void testIgnoreAnnotationsOnMethodAndReturnValue() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.method( "doSomething", String.class )
					.ignoreAnnotations( true )
					.returnValue()
						.ignoreAnnotations( false );
		config.addMapping( mapping );

		MethodDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForMethod( "doSomething", String.class );

		assertTrue( descriptor.hasConstrainedReturnValue(), "Setting given for return value should take precedence" );
	}

	@Test
	public void testIgnoreAnnotationsOnConstructor() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.constructor( String.class )
				.ignoreAnnotations( true );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class );

		assertNull( descriptor );
	}

	@Test
	public void testIgnoreAnnotationsOnTypeAndConstructor() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.ignoreAnnotations( true )
				.constructor( String.class )
					.ignoreAnnotations( false );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class );

		assertTrue( descriptor.hasConstrainedParameters() );
		assertTrue( descriptor.hasConstrainedReturnValue() );
	}

	@Test
	public void testIgnoreAnnotationsOnConstructorParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.constructor( String.class )
					.parameter( 0 )
					.ignoreAnnotations( true );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class );

		assertFalse( descriptor.hasConstrainedParameters() );
	}

	@Test
	public void testIgnoreAnnotationsOnConstructorAndParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();

		mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.constructor( String.class )
					.ignoreAnnotations( true )
					.parameter( 0 )
						.ignoreAnnotations( false );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class );

		assertTrue( descriptor.hasConstrainedParameters(), "Setting given for parameter should take precedence" );
	}

	@Test
	public void testIgnoreAnnotationsOnConstructorCrossParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.constructor( String.class, String.class )
					.crossParameter()
					.ignoreAnnotations( true );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class, String.class );

		assertNull( descriptor );
	}

	@Test
	public void testIgnoreAnnotationsOnConstructorAndCrossParameter() {
		ConstraintMapping mapping = config.createConstraintMapping();

		mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.constructor( String.class, String.class )
					.ignoreAnnotations( true )
					.crossParameter()
						.ignoreAnnotations( false );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class, String.class );

		assertTrue( descriptor.hasConstrainedParameters(), "Setting given for cross-parameter should take precedence" );
	}

	@Test
	public void testIgnoreAnnotationsOnConstructorReturnValue() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.constructor( String.class )
					.returnValue()
						.ignoreAnnotations( true );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class );

		assertFalse( descriptor.hasConstrainedReturnValue() );
	}

	@Test
	public void testIgnoreAnnotationsOnConstructorAndReturnValue() {
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Doer.class )
				.constructor( String.class )
					.ignoreAnnotations( true )
					.returnValue()
						.ignoreAnnotations( false );
		config.addMapping( mapping );

		ConstructorDescriptor descriptor = config.buildValidatorFactory()
				.getValidator()
				.getConstraintsForClass( Doer.class )
				.getConstraintsForConstructor( String.class );

		assertTrue( descriptor.hasConstrainedReturnValue(), "Setting given for return value should take precedence" );
	}

	@SuppressWarnings( "unused" )
	private static class Foo {
		@NotNull
		private String property;

		public String getProperty() {
			return property;
		}
	}

	private static class Doer {

		@NotNull
		public Doer(@NotNull String input) {
		}

		@GenericAndCrossParameterConstraint(validationAppliesTo = ConstraintTarget.PARAMETERS)
		public Doer(String input1, String input2) {
		}

		@NotNull
		public Object doSomething(@NotNull String input) {
			return null;
		}

		@GenericAndCrossParameterConstraint(validationAppliesTo = ConstraintTarget.PARAMETERS)
		public void doAnotherThing(String input) {
		}
	}

	private static class Bar {
		@NotNull
		private String property;
	}

	@Fighters
	private static class Fu {
	}

	@Constraint(validatedBy = { FightersValidator.class })
	@Target({ TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface Fighters {
		String message() default "fu fighters";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public class FightersValidator implements ConstraintValidator<Fighters, Object> {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}
}
