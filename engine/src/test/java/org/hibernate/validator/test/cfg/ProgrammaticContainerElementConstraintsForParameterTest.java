/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class ProgrammaticContainerElementConstraintsForParameterTest {

	private HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForParameterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test1", Optional.class, Map.class )
					.parameter( 0 )
						.containerElementType()
							.constraint( new SizeDef().max( 5 ) )
					.parameter( 1 )
						.containerElementType( 0 )
							.constraint( new SizeDef().min( 3 ).max( 10 ) )
					.containerElementType( 1 )
						.constraint( new MinDef().value( 1 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory()
				.getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			HashMap<String, Integer> fishCountByType = new HashMap<>();
			fishCountByType.put( "A", -1 );
			fishCountByType.put( "BB", -2 );

			fishTank.test1( Optional.of( "Too long" ), fishCountByType );

			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class ).withMessage( "size must be between 0 and 5" ),
					violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
					violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
					violationOf( Min.class ).withMessage( "must be greater than or equal to 1" ),
					violationOf( Min.class ).withMessage( "must be greater than or equal to 1" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareNestedContainerElementConstraintsForParameterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test2", Map.class )
					.parameter( 0 )
						.containerElementType( 1, 0 )
							.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			Map<String, List<Fish>> fishOfTheMonth = new HashMap<>();
			List<Fish> january = Arrays.asList( null, new Fish() );
			fishOfTheMonth.put( "january", january );

			fishTank.test2( fishOfTheMonth );

			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class ).withMessage( "must not be null" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareDeeplyNestedContainerElementConstraintsForParameterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test3", List.class )
					.parameter( 0 )
						.containerElementType( 0, 1, 0 )
							.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			Set<String> bobsTags = CollectionHelper.asSet( (String) null );
			Map<String, Set<String>> januaryTags = new HashMap<>();
			januaryTags.put( "bob", bobsTags );
			List<Map<String, Set<String>>> tagsOfFishOfTheMonth = new ArrayList<>();
			tagsOfFishOfTheMonth.add( januaryTags );

			fishTank.test3( tagsOfFishOfTheMonth );

			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class ).withMessage( "must not be null" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementCascadesForParameterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test4", Optional.class )
					.parameter( 0 )
						.containerElementType()
							.valid()
			.type( Fish.class )
				.field( "name" )
					.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			fishTank.test4( Optional.of( new Fish() ) );

			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class ).withMessage( "must not be null" )
			);
		}
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForArrayTypedParameterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test5", String[].class )
					.parameter( 0 )
						.containerElementType()
							.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			fishTank.test5( new String[] { "Too Long" } );

			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
			);
		}
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForListContainingArrayTypeParameterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test6", List.class )
					.parameter( 0 )
						.containerElementType( 0, 0 )
							.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			List<String[]> fishNamesByMonth = new ArrayList<>();
			fishNamesByMonth.add(  new String[] { "Too Long" } );

			fishTank.test6( fishNamesByMonth );

			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
			);
		}
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForMultiDimensionalArrayTypeParameterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test7", String[][].class )
					.parameter( 0 )
						.containerElementType( 0, 0 )
							.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			String[][] fishNamesByMonthAsArray = new String[][]{ new String[] { "Too Long" } };

			fishTank.test7( fishNamesByMonthAsArray );

			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
			);
		}
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000211.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintOnNonGenericParameterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "setSize", int.class )
					.parameter( 0 )
						.containerElementType( 1 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingTypeArgumentIndexOnParameterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.method( "test1", Optional.class, Map.class )
					.parameter( 0 )
						.containerElementType( 2 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingNestedTypeArgumentIndexOnParameterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.method( "test2", Map.class )
					.parameter( 0 )
					.containerElementType( 1, 2 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000213.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnParameterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.method( "test1", Optional.class, Map.class )
					.parameter( 1 )
						.containerElementType();
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000214.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void configuringSameContainerElementTwiceCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.method( "test3", List.class )
					.parameter( 0 )
						.containerElementType( 0, 1, 0 )
							.constraint( new NotNullDef() )
						.containerElementType( 0, 1, 0 );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030:.*")
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfListThrowsException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test8", List.class )
					.parameter( 0 )
						.containerElementType( 0 )
							.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
		fishTank.test8( Arrays.asList( "Too long" ) );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	//@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030:.*")
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfArrayThrowsException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( IFishTank.class )
				.method( "test9", Object[].class )
					.parameter( 0 )
						.containerElementType( 0 )
							.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
		fishTank.test9( new String[]{ "Too long" } );
	}

	public interface IFishTank {
		void test1(Optional<String> model, Map<String, Integer> fishCountByType);
		void test2(Map<String, List<Fish>> fishOfTheMonth);
		void test3(List<Map<String, Set<String>>> tagsOfFishOfTheMonth);
		void test4(Optional<Fish> boss);
		void test5(String[] fishNames);
		void test6(List<String[]> fishNamesByMonth);
		void test7(String[][] fishNamesByMonthAsArray);
		void setSize(int size);
		<T> void test8(List<T> fishNames);
		<T> void test9(T[] fishNames);
	}

	public static class FishTank implements IFishTank {

		@Override
		public void test1(Optional<String> model, Map<String, Integer> fishCountByType) {
		}

		@Override
		public void test2(Map<String, List<Fish>> fishOfTheMonth) {
		}

		@Override
		public void test3(List<Map<String, Set<String>>> tagsOfFishOfTheMonth) {
		}

		@Override
		public void test4(Optional<Fish> boss) {
		}

		@Override
		public void test5(String[] fishNames) {
		}

		@Override
		public void test6(List<String[]> fishNamesByMonth) {
		}

		@Override
		public void test7(String[][] fishNamesByMonthAsArray) {
		}

		@Override
		public <T> void test8(List<T> fishNames) {
		}

		@Override
		public <T> void test9(T[] fishNames) {
		}

		@Override
		public void setSize(int size) {
		}

		public FishTank() {
		}
	}

	public static class Fish {

		public Fish() {
		}

		public String name;
	}
}
