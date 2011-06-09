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

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.GroupDefinitionException;
import javax.validation.GroupSequence;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.context.impl.ConstraintMappingContext;
import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.FutureDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotEmptyDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.RangeDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ConstraintMappingTest {

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "The mapping cannot be null."
	)
	public void testNullConstraintMapping() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( (ConstraintMapping) null ).buildValidatorFactory();
	}

	@Test
	public void testConstraintMappingWithConstraintDefs() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() )
				.property( "numberOfHelpers", FIELD )
				.constraint( new MinDef().value( 1 ) );

		ConstraintMappingContext context = ConstraintMappingContext.getFromMapping( mapping );

		assertTrue( context.getConstraintConfig().containsKey( Marathon.class ) );
		assertTrue( context.getConstraintConfig().get( Marathon.class ).size() == 2 );
	}

	@Test
	public void testConstraintMappingWithGenericConstraints() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new GenericConstraintDef<NotNull>( NotNull.class ) )
				.property( "numberOfHelpers", FIELD )
				.constraint( new GenericConstraintDef<Min>( Min.class ).param( "value", 1 ) );

		ConstraintMappingContext context = ConstraintMappingContext.getFromMapping( mapping );

		assertTrue( context.getConstraintConfig().containsKey( Marathon.class ) );
		assertTrue( context.getConstraintConfig().get( Marathon.class ).size() == 2 );
	}

	@Test
	public void testDefConstraintFollowedByGenericConstraint() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "numberOfHelpers", FIELD )
				.constraint( new MinDef().value( 1 ) )
				.constraint( new GenericConstraintDef<Min>( Min.class ).param( "value", 1 ) );

		ConstraintMappingContext context = ConstraintMappingContext.getFromMapping( mapping );

		assertTrue( context.getConstraintConfig().containsKey( Marathon.class ) );
		assertTrue( context.getConstraintConfig().get( Marathon.class ).size() == 2 );
	}

	@Test
	public void testNoConstraintViolationForUnmappedEntity() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( new Marathon() );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	public void testSingleConstraint() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( new Marathon() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );
	}

	@Test
	public void testThatSpecificParameterCanBeSetAfterInvokingMethodFromBaseType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint(
						new SizeDef().message( "too short" ).min( 3 )
				);
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		Marathon marathon = new Marathon();
		marathon.setName( "NY" );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "too short" );
	}

	@Test(description = "HV-404: Introducing ConstraintsForType#genericConstraint(Class) allows to set specific parameters on following specific constraints.")
	public void testThatSpecificParameterCanBeSetAfterAddingGenericConstraintDef() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.constraint(
						new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class ).param( "minRunner", 1 )
				)
				.property( "name", METHOD )
				.constraint( new SizeDef().message( "name too short" ).min( 3 ) );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		Marathon marathon = new Marathon();
		marathon.setName( "NY" );
		marathon.addRunner( new Runner() );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "name too short" );
	}

	@Test
	public void testInheritedConstraint() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() )
				.type( Tournament.class )
				.property( "tournamentDate", METHOD )
				.constraint( new FutureDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

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
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "runners", METHOD )
				.valid()
				.type( Runner.class )
				.property( "paidEntryFee", FIELD )
				.constraint( new AssertTrueDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 0 );

		marathon.addRunner( new Runner() );
		violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be true" );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "The class class org.hibernate.validator.test.cfg.Marathon does not have a property 'numberOfHelpers' with access METHOD"
	)
	public void testSingleConstraintWrongAccessType() throws Throwable {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "numberOfHelpers", METHOD )
				.constraint( new NotNullDef() );
	}

	@Test
	public void testDefaultGroupSequence() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

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
	public void testDefaultGroupSequenceProvider() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.defaultGroupSequenceProvider( MarathonDefaultGroupSequenceProvider.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		Marathon marathon = new Marathon();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );

		marathon.setName( "Stockholm Marathon" );
		violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be empty" );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "Default group sequence and default group sequence provider cannot be defined at the same time"
	)
	public void testProgrammaticDefaultGroupSequenceAndDefaultGroupSequenceProviderDefinedOnSameClass() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.defaultGroupSequenceProvider( MarathonDefaultGroupSequenceProvider.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		validator.validate( new Marathon() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "Default group sequence and default group sequence provider cannot be defined at the same time"
	)
	public void testProgrammaticDefaultGroupSequenceDefinedOnClassWithGroupProviderAnnotation() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( B.class )
				.defaultGroupSequence( Foo.class, B.class )
				.property( "b", FIELD )
				.constraint( new NotNullDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		validator.validate( new B() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "Default group sequence and default group sequence provider cannot be defined at the same time"
	)
	public void testProgrammaticDefaultGroupSequenceProviderDefinedOnClassWithGroupSequenceAnnotation() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( A.class )
				.defaultGroupSequenceProvider( ADefaultGroupSequenceProvider.class )
				.property( "a", FIELD )
				.constraint( new NotNullDef() );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		validator.validate( new A() );
	}

	@Test
	public void testMultipleConstraintOfTheSameType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new SizeDef().min( 5 ) )
				.constraint( new SizeDef().min( 10 ) );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

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

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = ".*No value provided for minRunner.*"
	)
	public void testCustomConstraintTypeMissingParameter() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.constraint( new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class ) );

		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( mapping );
		config.buildValidatorFactory();
	}

	@Test
	public void testCustomConstraintType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.constraint(
						new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class )
								.param( "minRunner", 100 )
								.message( "Needs more runners" )
				);
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

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

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "The bean type must not be null when creating a constraint mapping."
	)
	public void testNullBean() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( null )
				.constraint( new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class ) );

		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( mapping ).buildValidatorFactory();
	}

	@Test(description = "HV-355 (parameter names of RangeDef wrong)")
	public void testRangeDef() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Runner.class )
				.property( "age", METHOD )
				.constraint( new RangeDef().min( 12 ).max( 99 ) );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		Set<ConstraintViolation<Runner>> violations = validator.validate( new Runner() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be between 12 and 99" );
	}

	@Test(description = "HV-444")
	public void testDefaultGroupSequenceDefinedOnClassWithNoConstraints() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() )
				.type( ExtendedMarathon.class )
				.defaultGroupSequence( Foo.class, ExtendedMarathon.class );
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );

		ExtendedMarathon extendedMarathon = new ExtendedMarathon();

		Set<ConstraintViolation<ExtendedMarathon>> violations = validator.validate( extendedMarathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );

		extendedMarathon.setName( "Stockholm Marathon" );
		violations = validator.validate( extendedMarathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be empty" );
	}

	@Test
	public void testProgrammaticAndAnnotationFieldConstraintsAddUp() {

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( User.class )
				.property( "firstName", ElementType.FIELD )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		Set<ConstraintViolation<User>> violations = validator.validateProperty( new User( "", "" ), "firstName" );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 1 and 10", "size must be between 2 and 10"
		);
	}

	@Test
	public void testProgrammaticAndAnnotationPropertyConstraintsAddUp() {

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( User.class )
				.property( "lastName", ElementType.METHOD )
				.constraint( new SizeDef().min( 4 ).max( 10 ) );

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		Set<ConstraintViolation<User>> violations = validator.validateProperty( new User( "", "" ), "lastName" );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 3 and 10", "size must be between 4 and 10"
		);
	}

	private interface Foo {
	}

	@GroupSequence( { Foo.class, A.class })
	private static class A {
		@SuppressWarnings("unused")
		String a;
	}

	@GroupSequenceProvider(BDefaultGroupSequenceProvider.class)
	private static class B {
		@SuppressWarnings("unused")
		String b;
	}

	private static class ExtendedMarathon extends Marathon {
	}

	@SuppressWarnings("unused")
	private static class User {

		@Size(min = 1, max = 10)
		private String firstName;

		private String lastName;

		private User(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		@Size(min = 3, max = 10)
		public String getLastName() {
			return lastName;
		}

	}

	public static class MarathonDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<Marathon> {
		public List<Class<?>> getValidationGroups(Marathon object) {
			return Arrays.<Class<?>>asList( Foo.class, Marathon.class );
		}
	}

	public static class BDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<B> {
		public List<Class<?>> getValidationGroups(B object) {
			return Arrays.<Class<?>>asList( Foo.class, B.class );
		}
	}

	public static class ADefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<A> {
		public List<Class<?>> getValidationGroups(A object) {
			return Arrays.<Class<?>>asList( Foo.class, A.class );
		}
	}
}
