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
import javax.validation.groups.Default;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.FutureDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotEmptyDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.RangeDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class ConstraintMappingTest {
	private HibernateValidatorConfiguration config;
	private DefaultConstraintMapping mapping;

	@BeforeMethod
	public void setUp() {
		config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		mapping = (DefaultConstraintMapping) config.createConstraintMapping();
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: The parameter \"mapping\" must not be null."
	)
	public void testNullConstraintMapping() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( (ConstraintMapping) null ).buildValidatorFactory();
	}

	@Test
	public void testConstraintMappingWithConstraintDefs() {
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() )
				.property( "numberOfHelpers", FIELD )
				.constraint( new MinDef().value( 1 ) );

		BeanConfiguration<Marathon> beanConfiguration = getBeanConfiguration( Marathon.class );
		assertNotNull( beanConfiguration );
		assertEquals( getConstrainedField( beanConfiguration, "numberOfHelpers" ).getConstraints().size(), 1 );
		assertEquals( getConstrainedExecutable( beanConfiguration, "getName" ).getConstraints().size(), 1 );
	}

	@Test
	public void testConstraintMappingWithGenericConstraints() {
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new GenericConstraintDef<NotNull>( NotNull.class ) )
				.property( "numberOfHelpers", FIELD )
				.constraint( new GenericConstraintDef<Min>( Min.class ).param( "value", 1L ) );

		BeanConfiguration<Marathon> beanConfiguration = getBeanConfiguration( Marathon.class );
		assertNotNull( beanConfiguration );
		assertEquals( getConstrainedField( beanConfiguration, "numberOfHelpers" ).getConstraints().size(), 1 );
		assertEquals( getConstrainedExecutable( beanConfiguration, "getName" ).getConstraints().size(), 1 );
	}

	@Test
	public void testDefConstraintFollowedByGenericConstraint() {
		mapping.type( Marathon.class )
				.property( "numberOfHelpers", FIELD )
				.constraint( new MinDef().value( 1 ) )
				.constraint( new GenericConstraintDef<Min>( Min.class ).param( "value", 2L ) );

		BeanConfiguration<Marathon> beanConfiguration = getBeanConfiguration( Marathon.class );
		assertNotNull( beanConfiguration );
		assertEquals( getConstrainedField( beanConfiguration, "numberOfHelpers" ).getConstraints().size(), 2 );
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
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( new Marathon() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );
	}

	@Test
	public void testThatSpecificParameterCanBeSetAfterInvokingMethodFromBaseType() {
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint(
						new SizeDef().message( "too short" ).min( 3 )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "NY" );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "too short" );
	}

	@Test(description = "HV-404: Introducing ConstraintsForType#genericConstraint(Class) allows to set specific parameters on following specific constraints.")
	public void testThatSpecificParameterCanBeSetAfterAddingGenericConstraintDef() {
		mapping.type( Marathon.class )
				.constraint(
						new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class ).param( "minRunner", 1 )
				)
				.property( "name", METHOD )
				.constraint( new SizeDef().message( "name too short" ).min( 3 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "NY" );
		marathon.addRunner( new Runner() );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "name too short" );
	}

	@Test
	public void testInheritedConstraint() {
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() )
				.type( Tournament.class )
				.property( "tournamentDate", METHOD )
				.constraint( new FutureDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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
		mapping.type( Marathon.class )
				.property( "runners", METHOD )
				.valid()
				.type( Runner.class )
				.property( "paidEntryFee", FIELD )
				.constraint( new AssertTrueDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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
	public void testValidWithGroupConversion() {
		mapping.type( Marathon.class )
				.property( "runners", METHOD )
				.valid()
				.convertGroup( Default.class ).to( Foo.class )
				.type( Runner.class )
				.property( "paidEntryFee", FIELD )
				.constraint( new AssertTrueDef().groups( Foo.class ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );

		marathon.addRunner( new Runner() );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be true" );
	}

	@Test
	public void testValidWithSeveralGroupConversions() {
		mapping.type( Marathon.class )
				.property( "runners", METHOD )
				.valid()
				.convertGroup( Default.class ).to( Foo.class )
				.convertGroup( Bar.class ).to( Default.class )
				.type( Runner.class )
				.property( "paidEntryFee", FIELD )
				.constraint( new AssertTrueDef().groups( Foo.class ) )
				.constraint( new AssertTrueDef().message( "really, it must be true" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );

		marathon.addRunner( new Runner() );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon, Default.class, Bar.class );
		assertNumberOfViolations( violations, 2 );
		assertCorrectConstraintViolationMessages( violations, "must be true", "really, it must be true" );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: The class class org.hibernate.validator.test.cfg.Marathon does not have a property 'numberOfHelpers' with access METHOD."
	)
	public void testSingleConstraintWrongAccessType() throws Throwable {
		mapping.type( Marathon.class )
				.property( "numberOfHelpers", METHOD )
				.constraint( new NotNullDef() );
	}

	@Test
	public void testDefaultGroupSequence() {
		mapping.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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
		mapping.type( Marathon.class )
				.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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
			expectedExceptionsMessageRegExp = "HV[0-9]*: Default group sequence and default group sequence provider cannot be defined at the same time."
	)
	public void testProgrammaticDefaultGroupSequenceAndDefaultGroupSequenceProviderDefinedOnSameClass() {
		mapping.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new Marathon() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Default group sequence and default group sequence provider cannot be defined at the same time."
	)
	public void testProgrammaticDefaultGroupSequenceDefinedOnClassWithGroupProviderAnnotation() {
		mapping.type( B.class )
				.defaultGroupSequence( Foo.class, B.class )
				.property( "b", FIELD )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new B() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Default group sequence and default group sequence provider cannot be defined at the same time."
	)
	public void testProgrammaticDefaultGroupSequenceProviderDefinedOnClassWithGroupSequenceAnnotation() {
		mapping.type( A.class )
				.defaultGroupSequenceProviderClass( ADefaultGroupSequenceProvider.class )
				.property( "a", FIELD )
				.constraint( new NotNullDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new A() );
	}

	@Test
	public void testMultipleConstraintOfTheSameType() {
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new SizeDef().min( 5 ) )
				.constraint( new SizeDef().min( 10 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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
			expectedExceptionsMessageRegExp = "HV000012.*"
	)
	public void testCustomConstraintTypeMissingParameter() {
		mapping.type( Marathon.class )
				.constraint( new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class ) );
		config.addMapping( mapping );
		config.buildValidatorFactory().getValidator();
	}

	@Test
	public void testCustomConstraintType() {
		mapping.type( Marathon.class )
				.constraint(
						new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class )
								.param( "minRunner", 100 )
								.message( "Needs more runners" )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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
			expectedExceptionsMessageRegExp = "HV[0-9]*: The bean type must not be null when creating a constraint mapping."
	)
	public void testNullBean() {
		mapping.type( null )
				.constraint( new GenericConstraintDef<MarathonConstraint>( MarathonConstraint.class ) );
		config.addMapping( mapping ).buildValidatorFactory();
	}

	@Test(description = "HV-355 (parameter names of RangeDef wrong)")
	public void testRangeDef() {
		mapping.type( Runner.class )
				.property( "age", METHOD )
				.constraint( new RangeDef().min( 12 ).max( 99 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Runner>> violations = validator.validate( new Runner() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be between 12 and 99" );
	}

	@Test(description = "HV-444")
	public void testDefaultGroupSequenceDefinedOnClassWithNoConstraints() {
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().groups( Foo.class ) )
				.property( "runners", METHOD )
				.constraint( new NotEmptyDef() )
				.type( ExtendedMarathon.class )
				.defaultGroupSequence( Foo.class, ExtendedMarathon.class );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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
		mapping.type( User.class )
				.property( "firstName", ElementType.FIELD )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<User>> violations = validator.validateProperty( new User( "", "" ), "firstName" );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 1 and 10", "size must be between 2 and 10"
		);
	}

	@Test
	public void testProgrammaticAndAnnotationPropertyConstraintsAddUp() {
		mapping.type( User.class )
				.property( "lastName", ElementType.METHOD )
				.constraint( new SizeDef().min( 4 ).max( 10 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<User>> violations = validator.validateProperty( new User( "", "" ), "lastName" );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 3 and 10", "size must be between 4 and 10"
		);
	}

	private <T> BeanConfiguration<T> getBeanConfiguration(Class<T> type) {
		Set<BeanConfiguration<?>> beanConfigurations = mapping.getBeanConfigurations(
				new ConstraintHelper(),
				new DefaultParameterNameProvider()
		);

		for ( BeanConfiguration<?> beanConfiguration : beanConfigurations ) {
			if ( beanConfiguration.getBeanClass() == type ) {
				@SuppressWarnings("unchecked")
				BeanConfiguration<T> configuration = (BeanConfiguration<T>) beanConfiguration;
				return configuration;
			}
		}

		return null;
	}

	private ConstrainedField getConstrainedField(BeanConfiguration<?> beanConfiguration, String fieldName) {
		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.FIELD &&
					constrainedElement.getLocation().getMember().getName().equals( fieldName ) ) {
				return (ConstrainedField) constrainedElement;
			}
		}

		return null;
	}

	private ConstrainedExecutable getConstrainedExecutable(BeanConfiguration<?> beanConfiguration, String executableName) {
		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD &&
					constrainedElement.getLocation().getMember().getName().equals( executableName ) ) {
				return (ConstrainedExecutable) constrainedElement;
			}
		}

		return null;
	}

	private interface Foo {
	}

	private interface Bar {
	}

	@GroupSequence({ Foo.class, A.class })
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
		private final String firstName;

		private final String lastName;

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
		@Override
		public List<Class<?>> getValidationGroups(Marathon object) {
			return Arrays.<Class<?>>asList( Foo.class, Marathon.class );
		}
	}

	public static class BDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<B> {
		@Override
		public List<Class<?>> getValidationGroups(B object) {
			return Arrays.<Class<?>>asList( Foo.class, B.class );
		}
	}

	public static class ADefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<A> {
		@Override
		public List<Class<?>> getValidationGroups(A object) {
			return Arrays.<Class<?>>asList( Foo.class, A.class );
		}
	}
}
