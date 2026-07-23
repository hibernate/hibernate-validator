/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.test.cfg;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

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

		HashMap<String, Integer> fishCountByType = new HashMap<>();
		fishCountByType.put( "A", -1 );
		fishCountByType.put( "BB", -2 );

		assertThatThrownBy( () -> fishTank.test1( Optional.of( "Too long" ), fishCountByType ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( Size.class ).withMessage( "size must be between 0 and 5" ),
						violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
						violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
						violationOf( Min.class ).withMessage( "must be greater than or equal to 1" ),
						violationOf( Min.class ).withMessage( "must be greater than or equal to 1" )
				) );
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

		Map<String, List<Fish>> fishOfTheMonth = new HashMap<>();
		List<Fish> january = Arrays.asList( null, new Fish() );
		fishOfTheMonth.put( "january", january );

		assertThatThrownBy( () -> fishTank.test2( fishOfTheMonth ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( NotNull.class ).withMessage( "must not be null" )
				) );
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

		Set<String> bobsTags = CollectionHelper.asSet( (String) null );
		Map<String, Set<String>> januaryTags = new HashMap<>();
		januaryTags.put( "bob", bobsTags );
		List<Map<String, Set<String>>> tagsOfFishOfTheMonth = new ArrayList<>();
		tagsOfFishOfTheMonth.add( januaryTags );

		assertThatThrownBy( () -> fishTank.test3( tagsOfFishOfTheMonth ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( NotNull.class ).withMessage( "must not be null" )
				) );
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

		assertThatThrownBy( () -> fishTank.test4( Optional.of( new Fish() ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( NotNull.class ).withMessage( "must not be null" )
				) );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForArrayTypedParameterProgrammatically() {
		assertThatThrownBy( () -> {
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

			fishTank.test5( new String[] { "Too Long" } );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForListContainingArrayTypeParameterProgrammatically() {
		assertThatThrownBy( () -> {
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

			List<String[]> fishNamesByMonth = new ArrayList<>();
			fishNamesByMonth.add( new String[] { "Too Long" } );

			fishTank.test6( fishNamesByMonth );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForMultiDimensionalArrayTypeParameterProgrammatically() {
		assertThatThrownBy( () -> {
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

			String[][] fishNamesByMonthAsArray = new String[][] { new String[] { "Too Long" } };

			fishTank.test7( fishNamesByMonthAsArray );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintOnNonGenericParameterCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( IFishTank.class )
					.method( "setSize", int.class )
					.parameter( 0 )
					.containerElementType( 1 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000211.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingTypeArgumentIndexOnParameterCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( FishTank.class )
					.method( "test1", Optional.class, Map.class )
					.parameter( 0 )
					.containerElementType( 2 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000212.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingNestedTypeArgumentIndexOnParameterCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( FishTank.class )
					.method( "test2", Map.class )
					.parameter( 0 )
					.containerElementType( 1, 2 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000212.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnParameterCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( FishTank.class )
					.method( "test1", Optional.class, Map.class )
					.parameter( 1 )
					.containerElementType();
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000213.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void configuringSameContainerElementTwiceCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( FishTank.class )
					.method( "test3", List.class )
					.parameter( 0 )
					.containerElementType( 0, 1, 0 )
					.constraint( new NotNullDef() )
					.containerElementType( 0, 1, 0 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000214.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfListThrowsException() {
		assertThatThrownBy( () -> {
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
		} ).isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	//@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030:.*")
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfArrayThrowsException() {
		assertThatThrownBy( () -> {
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
			fishTank.test9( new String[] { "Too long" } );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
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
