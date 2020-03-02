/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintCreationContext;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.GroupDefinitionException;
import jakarta.validation.GroupSequence;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

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
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
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
				.getter( "name" )
					.constraint( new NotNullDef() )
				.field( "numberOfHelpers" )
					.constraint( new MinDef().value( 1 ) );

		BeanConfiguration<Marathon> beanConfiguration = getBeanConfiguration( Marathon.class );
		assertNotNull( beanConfiguration );
		assertEquals( getConstrainedField( beanConfiguration, "numberOfHelpers" ).getConstraints().size(), 1 );
		assertEquals( getConstrainedExecutable( beanConfiguration, "getName" ).getConstraints().size(), 1 );
	}

	@Test
	public void testConstraintMappingWithGenericConstraints() {
		mapping.type( Marathon.class )
				.getter( "name" )
					.constraint( new GenericConstraintDef<>( NotNull.class ) )
				.field( "numberOfHelpers" )
					.constraint( new GenericConstraintDef<>( Min.class ).param( "value", 1L ) );

		BeanConfiguration<Marathon> beanConfiguration = getBeanConfiguration( Marathon.class );
		assertNotNull( beanConfiguration );
		assertEquals( getConstrainedField( beanConfiguration, "numberOfHelpers" ).getConstraints().size(), 1 );
		assertEquals( getConstrainedExecutable( beanConfiguration, "getName" ).getConstraints().size(), 1 );
	}

	@Test
	public void testDefConstraintFollowedByGenericConstraint() {
		mapping.type( Marathon.class )
				.field( "numberOfHelpers" )
					.constraint( new MinDef().value( 1 ) )
					.constraint( new GenericConstraintDef<>( Min.class ).param( "value", 2L ) );

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
		assertNoViolations( violations );
	}

	@Test
	public void testSingleConstraint() {
		mapping.type( Marathon.class )
				.getter( "name" )
					.constraint( new NotNullDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( new Marathon() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	@Test
	public void testThatSpecificParameterCanBeSetAfterInvokingMethodFromBaseType() {
		mapping.type( Marathon.class )
				.getter( "name" )
					.constraint( new SizeDef().message( "too short" ).min( 3 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "NY" );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "too short" )
		);
	}

	@Test(description = "HV-404: Introducing ConstraintsForType#genericConstraint(Class) allows to set specific parameters on following specific constraints.")
	public void testThatSpecificParameterCanBeSetAfterAddingGenericConstraintDef() {
		mapping.type( Marathon.class )
					.constraint( new GenericConstraintDef<>( MarathonConstraint.class ).param( "minRunner", 1 ) )
				.getter( "name" )
					.constraint( new SizeDef().message( "name too short" ).min( 3 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "NY" );
		marathon.addRunner( new Runner() );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "name too short" )
		);
	}

	@Test
	public void testInheritedConstraint() {
		mapping.type( Marathon.class )
				.getter( "name" )
					.constraint( new NotNullDef() )
				.type( Tournament.class )
					.getter( "tournamentDate" )
						.constraint( new FutureDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );
		Calendar cal = GregorianCalendar.getInstance();
		cal.set( Calendar.YEAR, -1 );
		marathon.setTournamentDate( cal.getTime() );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Future.class ).withMessage( "must be a future date" )
		);
	}

	@Test
	public void testValid() {
		mapping.type( Marathon.class )
				.getter( "runners" )
					.valid()
				.type( Runner.class )
					.field( "paidEntryFee" )
						.constraint( new AssertTrueDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertNoViolations( violations );

		marathon.addRunner( new Runner() );
		violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( AssertTrue.class ).withMessage( "must be true" )
		);
	}

	@Test
	public void testValidWithGroupConversion() {
		mapping.type( Marathon.class )
				.getter( "runners" )
					.valid()
					.convertGroup( Default.class ).to( Foo.class )
				.type( Runner.class )
					.field( "paidEntryFee" )
						.constraint( new AssertTrueDef().groups( Foo.class ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );

		marathon.addRunner( new Runner() );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( AssertTrue.class ).withMessage( "must be true" )
		);
	}

	@Test
	public void testValidWithSeveralGroupConversions() {
		mapping.type( Marathon.class )
				.getter( "runners" )
					.valid()
					.convertGroup( Default.class ).to( Foo.class )
					.convertGroup( Bar.class ).to( Default.class )
				.type( Runner.class )
					.field( "paidEntryFee" )
						.constraint( new AssertTrueDef().groups( Foo.class ) )
						.constraint( new AssertTrueDef().message( "really, it must be true" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "New York Marathon" );

		marathon.addRunner( new Runner() );
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon, Default.class, Bar.class );
		assertThat( violations ).containsOnlyViolations(
				violationOf( AssertTrue.class ).withMessage( "must be true" ),
				violationOf( AssertTrue.class ).withMessage( "really, it must be true" )
		);
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000013.*"
	)
	public void testSingleConstraintWrongAccessType() throws Throwable {
		mapping.type( Marathon.class )
				.getter( "numberOfHelpers" )
					.constraint( new NotNullDef() );
	}

	@Test
	public void testDefaultGroupSequence() {
		mapping.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.getter( "name" )
					.constraint( new NotNullDef().groups( Foo.class ) )
				.getter( "runners" )
					.constraint( new NotEmptyDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);

		marathon.setName( "Stockholm Marathon" );
		violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotEmpty.class ).withMessage( "must not be empty" )
		);
	}

	@Test
	public void testDefaultGroupSequenceProvider() {
		mapping.type( Marathon.class )
				.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class )
				.getter( "name" )
					.constraint( new NotNullDef().groups( Foo.class ) )
				.getter( "runners" )
					.constraint( new NotEmptyDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);

		marathon.setName( "Stockholm Marathon" );
		violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotEmpty.class ).withMessage( "must not be empty" )
		);
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Default group sequence and default group sequence provider cannot be defined at the same time."
	)
	public void testProgrammaticDefaultGroupSequenceAndDefaultGroupSequenceProviderDefinedOnSameClass() {
		mapping.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class )
				.getter( "name" )
					.constraint( new NotNullDef().groups( Foo.class ) )
				.getter( "runners" )
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
				.field( "b" )
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
				.field( "a" )
					.constraint( new NotNullDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new A() );
	}

	@Test
	public void testMultipleConstraintOfTheSameType() {
		mapping.type( Marathon.class )
				.getter( "name" )
					.constraint( new SizeDef().min( 5 ) )
					.constraint( new SizeDef().min( 10 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "Foo" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 10 and 2147483647" ),
				violationOf( Size.class ).withMessage( "size must be between 5 and 2147483647" )
		);

		marathon.setName( "Foobar" );
		violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 10 and 2147483647" )
		);

		marathon.setName( "Stockholm Marathon" );
		violations = validator.validate( marathon );
		assertNoViolations( violations );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000012.*"
	)
	public void testCustomConstraintTypeMissingParameter() {
		mapping.type( Marathon.class )
				.constraint( new GenericConstraintDef<>( MarathonConstraint.class ) );
		config.addMapping( mapping );
		config.buildValidatorFactory().getValidator();
	}

	@Test
	public void testCustomConstraintType() {
		mapping.type( Marathon.class )
				.constraint(
						new GenericConstraintDef<>( MarathonConstraint.class )
								.param( "minRunner", 100 )
								.message( "Needs more runners" )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "Stockholm Marathon" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( MarathonConstraint.class ).withMessage( "Needs more runners" )
		);

		for ( int i = 0; i < 100; i++ ) {
			marathon.addRunner( new Runner() );
		}
		violations = validator.validate( marathon );
		assertNoViolations( violations );
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: The bean type must not be null when creating a constraint mapping."
	)
	public void testNullBean() {
		mapping.type( null )
				.constraint( new GenericConstraintDef<>( MarathonConstraint.class ) );
		config.addMapping( mapping ).buildValidatorFactory();
	}

	@Test(description = "HV-355 (parameter names of RangeDef wrong)")
	public void testRangeDef() {
		mapping.type( Runner.class )
				.getter( "age" )
					.constraint( new RangeDef().min( 12 ).max( 99 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Runner>> violations = validator.validate( new Runner() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Range.class ).withMessage( "must be between 12 and 99" )
		);
	}

	@Test(description = "HV-444")
	public void testDefaultGroupSequenceDefinedOnClassWithNoConstraints() {
		mapping.type( Marathon.class )
				.getter( "name" )
					.constraint( new NotNullDef().groups( Foo.class ) )
				.getter( "runners" )
					.constraint( new NotEmptyDef() )
				.type( ExtendedMarathon.class )
					.defaultGroupSequence( Foo.class, ExtendedMarathon.class );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		ExtendedMarathon extendedMarathon = new ExtendedMarathon();

		Set<ConstraintViolation<ExtendedMarathon>> violations = validator.validate( extendedMarathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);

		extendedMarathon.setName( "Stockholm Marathon" );
		violations = validator.validate( extendedMarathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotEmpty.class ).withMessage( "must not be empty" )
		);
	}

	@Test
	public void testProgrammaticAndAnnotationFieldConstraintsAddUp() {
		mapping.type( User.class )
				.field( "firstName" )
					.constraint( new SizeDef().min( 2 ).max( 10 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<User>> violations = validator.validateProperty( new User( "", "" ), "firstName" );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 1 and 10" ),
				violationOf( Size.class ).withMessage( "size must be between 2 and 10" )
		);
	}

	@Test
	public void testProgrammaticAndAnnotationPropertyConstraintsAddUp() {
		mapping.type( User.class )
				.getter( "lastName" )
					.constraint( new SizeDef().min( 4 ).max( 10 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<User>> violations = validator.validateProperty( new User( "", "" ), "lastName" );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
				violationOf( Size.class ).withMessage( "size must be between 4 and 10" )
		);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testDeprecatedPropertyMethodForFieldAndGetterProgrammaticConstraintDefinition() {
		mapping.type( Marathon.class )
				.property( "name", ElementType.METHOD )
					.constraint( new SizeDef().min( 5 ) )
					.constraint( new SizeDef().min( 10 ) )
				.property( "runners", ElementType.FIELD )
					.constraint( new SizeDef().max( 10 ).min( 1 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Marathon marathon = new Marathon();
		marathon.setName( "Foo" );

		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 10 and 2147483647" ),
				violationOf( Size.class ).withMessage( "size must be between 5 and 2147483647" ),
				violationOf( Size.class ).withMessage( "size must be between 1 and 10" )
		);
	}

	private <T> BeanConfiguration<T> getBeanConfiguration(Class<T> type) {
		Set<BeanConfiguration<?>> beanConfigurations = mapping.getBeanConfigurations(
				getDummyConstraintCreationContext()
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
					( (ConstrainedField) constrainedElement ).getField().getPropertyName().equals( fieldName ) ) {
				return (ConstrainedField) constrainedElement;
			}
		}

		return null;
	}

	private ConstrainedExecutable getConstrainedExecutable(BeanConfiguration<?> beanConfiguration, String executableName) {
		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( constrainedElement.getKind().isMethod() &&
					( (ConstrainedExecutable) constrainedElement ).getCallable().getName().equals( executableName ) ) {
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
