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

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.ConstraintsForType;
import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.FutureDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotEmptyDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.RangeDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.test.util.TestUtil;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.test.util.TestUtil.assertConstraintViolation;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
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
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( (ConstraintMapping) null ).buildValidatorFactory();
	}

	@Test
	public void testConstraintMappingWithConstraintDefs() {
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
	public void testConstraintMappingWithGenericConstraints() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.genericConstraint( NotNull.class )
				.property( "numberOfHelpers", FIELD )
				.genericConstraint( Min.class ).param( "value", 1 );

		assertTrue( mapping.getConstraintConfig().containsKey( Marathon.class ) );
		assertTrue( mapping.getConstraintConfig().get( Marathon.class ).size() == 2 );
	}

	@Test
	public void testDefConstraintFollowedByGenericConstraint() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "numberOfHelpers", FIELD )
				.constraint( MinDef.class ).value( 1 )
				.genericConstraint( Min.class ).param( "value", 1 );

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
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( new Marathon() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );
	}

	@Test
	public void testThatSpecificParameterCanBeSetAfterInvokingMethodFromBaseType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( SizeDef.class )
				.message( "too short" )
				.min( 3 );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

		Marathon marathon = new Marathon();
		marathon.setName( "NY" );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "too short" );
	}

	/**
	 * HV-404: Introducing {@link ConstraintsForType#genericConstraint(Class)} allows to set
	 * specific parameters on following specific constraints.
	 */
	@Test
	public void testThatSpecificParameterCanBeSetAfterAddingGenericConstraintDef() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.genericConstraint( MarathonConstraint.class )
				.param( "minRunner", 1 )
				.property( "name", METHOD )
				.constraint( SizeDef.class )
				.message( "name too short" )
				.min( 3 );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
		mapping
				.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class )
				.type( Tournament.class )
				.property( "tournamentDate", METHOD )
				.constraint( FutureDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
				.valid( "runners", METHOD )
				.type( Runner.class )
				.property( "paidEntryFee", FIELD )
				.constraint( AssertTrueDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
		try {
			mapping
					.type( Marathon.class )
					.property( "numberOfHelpers", METHOD )
					.constraint( NotNullDef.class );
		}
		catch ( ValidationException e ) {
			throw ( e.getCause().getCause() );
		}
	}

	@Test
	public void testDefaultGroupSequence() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping
				.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class ).groups( Foo.class )
				.property( "runners", METHOD )
				.constraint( NotEmptyDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
		mapping
				.type( Marathon.class )
				.defaultGroupSequenceProvider( MarathonDefaultGroupSequenceProvider.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class ).groups( Foo.class )
				.property( "runners", METHOD )
				.constraint( NotEmptyDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
			expectedExceptionsMessageRegExp = "The default group sequence provider defined for .* has the wrong type"
	)
	public void testDefaultGroupSequenceProviderDefinedWithWrongType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping
				.type( Marathon.class )
				.defaultGroupSequenceProvider( BDefaultGroupSequenceProvider.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

		validator.validate( new Marathon() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "The default group sequence provider defined for .* must be an implementation of the DefaultGroupSequenceProvider interface"
	)
	public void testDefaultGroupSequenceProviderDefinedWithInterface() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping
				.type( Marathon.class )
				.defaultGroupSequenceProvider( NoImplDefaultGroupSequenceProvider.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

		validator.validate( new Marathon() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "Default group sequence and default group sequence provider cannot be defined at the same time"
	)
	public void testProgrammaticDefaultGroupSequenceAndDefaultGroupSequenceProviderDefinedOnSameClass() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping
				.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.defaultGroupSequenceProvider( MarathonDefaultGroupSequenceProvider.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class ).groups( Foo.class )
				.property( "runners", METHOD )
				.constraint( NotEmptyDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
				.constraint( NotNullDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
				.constraint( NotNullDef.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

		validator.validate( new A() );
	}

	@Test
	public void testMultipleConstraintOfTheSameType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( SizeDef.class ).min( 5 )
				.constraint( SizeDef.class ).min( 10 );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
				.genericConstraint( MarathonConstraint.class );

		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( mapping );
		config.buildValidatorFactory();
	}

	@Test
	public void testCustomConstraintType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Marathon.class )
				.genericConstraint( MarathonConstraint.class )
				.param( "minRunner", 100 )
				.message( "Needs more runners" );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

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
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "Null is not a valid bean type"
	)
	public void testNullBean() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( null )
				.genericConstraint( MarathonConstraint.class );

		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		config.addMapping( mapping ).buildValidatorFactory();
	}

	@Test(description = "HV-355 (parameter names of RangeDef wrong)")
	public void testRangeDef() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Runner.class )
				.property( "age", METHOD )
				.constraint( RangeDef.class )
				.min( 12 )
				.max( 99 );
		Validator validator = TestUtil.getValidatorForMapping( mapping );
		Set<ConstraintViolation<Runner>> violations = validator.validate( new Runner() );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "must be between 12 and 99" );
	}

	@Test(description = "HV-444")
	public void testDefaultGroupSequenceDefinedOnClassWithNoConstraints() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping
				.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( NotNullDef.class ).groups( Foo.class )
				.property( "runners", METHOD )
				.constraint( NotEmptyDef.class )
				.type( ExtendedMarathon.class )
				.defaultGroupSequence( Foo.class, ExtendedMarathon.class );
		Validator validator = TestUtil.getValidatorForMapping( mapping );

		ExtendedMarathon extendedMarathon = new ExtendedMarathon();

		Set<ConstraintViolation<ExtendedMarathon>> violations = validator.validate( extendedMarathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be null" );

		extendedMarathon.setName( "Stockholm Marathon" );
		violations = validator.validate( extendedMarathon );
		assertNumberOfViolations( violations, 1 );
		assertConstraintViolation( violations.iterator().next(), "may not be empty" );
	}

	private interface Foo {
	}

	@GroupSequence( { Foo.class, A.class })
	private static class A {
		String a;
	}

	@GroupSequenceProvider(BDefaultGroupSequenceProvider.class)
	private static class B {
		String b;
	}

	private static class ExtendedMarathon extends Marathon {
	}

	public static class MarathonDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<Marathon> {
		public List<Class<?>> getValidationGroups(Marathon object) {
			return Arrays.asList( Foo.class, Marathon.class );
		}
	}

	public static class BDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<B> {
		public List<Class<?>> getValidationGroups(B object) {
			return Arrays.asList( Foo.class, B.class );
		}
	}

	public static class ADefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<A> {
		public List<Class<?>> getValidationGroups(A object) {
			return Arrays.asList( Foo.class, A.class );
		}
	}

	public static interface NoImplDefaultGroupSequenceProvider extends DefaultGroupSequenceProvider<Marathon> {
	}
}


