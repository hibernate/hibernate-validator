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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Gunnar Morling
 */
public class ProgrammaticContainerElementConstraintsForReturnValueTest {

	private HibernateValidatorConfiguration config;

	@BeforeEach
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForReturnValueProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( IFishTank.class )
				.method( "test1" )
				.returnValue()
				.containerElementType( 0 )
				.constraint( new SizeDef().min( 3 ).max( 10 ) )
				.containerElementType( 1 )
				.constraint( new MinDef().value( 1 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory()
				.getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		assertThatThrownBy( () -> fishTank.test1() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
						violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
						violationOf( Min.class ).withMessage( "must be greater than or equal to 1" ),
						violationOf( Min.class ).withMessage( "must be greater than or equal to 1" )
				) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareNestedContainerElementConstraintsForReturnValueProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( IFishTank.class )
				.method( "test2" )
				.returnValue()
				.containerElementType( 1, 0 )
				.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		assertThatThrownBy( () -> fishTank.test2() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( NotNull.class ).withMessage( "must not be null" )
				) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareDeeplyNestedContainerElementConstraintsForReturnValueProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( IFishTank.class )
				.method( "test3" )
				.returnValue()
				.containerElementType( 0, 1, 0 )
				.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		assertThatThrownBy( () -> fishTank.test3() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( NotNull.class ).withMessage( "must not be null" )
				) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementCascadesForReturnValueProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( IFishTank.class )
				.method( "test4" )
				.returnValue()
				.containerElementType()
				.valid()
				.type( Fish.class )
				.field( "name" )
				.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		assertThatThrownBy( () -> fishTank.test4() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat( ( (ConstraintViolationException) e ).getConstraintViolations() ).containsOnlyViolations(
						violationOf( NotNull.class ).withMessage( "must not be null" )
				) );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForArrayTypedReturnValueProgrammatically() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( IFishTank.class )
					.method( "test5" )
					.returnValue()
					.containerElementType()
					.constraint( new SizeDef().max( 5 ) );

			config.addMapping( newMapping );
			Validator validator = config.buildValidatorFactory().getValidator();

			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

			fishTank.test5();
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForListContainingArrayTypeReturnValueProgrammatically() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( IFishTank.class )
					.method( "test6" )
					.returnValue()
					.containerElementType( 0, 0 )
					.constraint( new SizeDef().max( 5 ) );

			config.addMapping( newMapping );
			Validator validator = config.buildValidatorFactory().getValidator();

			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

			fishTank.test6();
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForMultiDimensionalArrayTypeReturnValueProgrammatically() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( IFishTank.class )
					.method( "test7" )
					.returnValue()
					.containerElementType( 0, 0 )
					.constraint( new SizeDef().max( 5 ) );

			config.addMapping( newMapping );
			Validator validator = config.buildValidatorFactory().getValidator();

			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

			fishTank.test7();
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintOnNonGenericReturnValueCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( IFishTank.class )
					.method( "getSize" )
					.returnValue()
					.containerElementType( 1 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000211.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingTypeArgumentIndexOnReturnValueCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( FishTank.class )
					.method( "test1" )
					.returnValue()
					.containerElementType( 2 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000212.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingNestedTypeArgumentIndexOnReturnValueCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( FishTank.class )
					.method( "test2" )
					.returnValue()
					.containerElementType( 1, 2 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000212.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnReturnValueCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping newMapping = config.createConstraintMapping();
			newMapping
					.type( FishTank.class )
					.method( "test1" )
					.returnValue()
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
					.method( "test3" )
					.returnValue()
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
					.returnValue()
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
					.returnValue()
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
		Map<String, Integer> test1();

		Map<String, List<Fish>> test2();

		List<Map<String, Set<String>>> test3();

		Optional<Fish> test4();

		String[] test5();

		List<String[]> test6();

		String[][] test7();

		<T> List<T> test8(List<T> list);

		<T> T[] test9(T[] array);

		int getSize();
	}

	public static class FishTank implements IFishTank {

		@Override
		public Map<String, Integer> test1() {
			HashMap<String, Integer> fishCountByType = new HashMap<>();
			fishCountByType.put( "A", -1 );
			fishCountByType.put( "BB", -2 );

			return fishCountByType;
		}

		@Override
		public Map<String, List<Fish>> test2() {
			Map<String, List<Fish>> fishOfTheMonth = new HashMap<>();
			List<Fish> january = Arrays.asList( null, new Fish() );
			fishOfTheMonth.put( "january", january );

			return fishOfTheMonth;
		}

		@Override
		public List<Map<String, Set<String>>> test3() {
			Set<String> bobsTags = CollectionHelper.asSet( (String) null );
			Map<String, Set<String>> januaryTags = new HashMap<>();
			januaryTags.put( "bob", bobsTags );
			List<Map<String, Set<String>>> tagsOfFishOfTheMonth = new ArrayList<>();
			tagsOfFishOfTheMonth.add( januaryTags );

			return tagsOfFishOfTheMonth;
		}

		@Override
		public Optional<Fish> test4() {
			return Optional.of( new Fish() );
		}

		@Override
		public String[] test5() {
			return new String[] { "Too Long" };
		}

		@Override
		public List<String[]> test6() {
			List<String[]> fishNamesByMonth = new ArrayList<>();
			fishNamesByMonth.add( new String[] { "Too Long" } );
			return fishNamesByMonth;
		}

		@Override
		public String[][] test7() {
			return new String[][] { new String[] { "Too Long" } };
		}

		@Override
		public <T> List<T> test8(List<T> list) {
			return list;
		}

		@Override
		public <T> T[] test9(T[] array) {
			return array;
		}

		@Override
		public int getSize() {
			return 0;
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
