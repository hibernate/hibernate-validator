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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProgrammaticContainerElementConstraintsForGetterTest {

	private HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForGetterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "model" )
					.containerElementType()
						.constraint( new SizeDef().max( 5 ) )
				.getter( "fishCountByType" )
					.containerElementType( 0 )
						.constraint( new SizeDef().min( 3 ).max( 10 ) )
					.containerElementType( 1 )
						.constraint( new MinDef().value( 1 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" ),
				violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
				violationOf( Size.class ).withMessage( "size must be between 3 and 10" ),
				violationOf( Min.class ).withMessage( "must be greater than or equal to 1" ),
				violationOf( Min.class ).withMessage( "must be greater than or equal to 1" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareNestedContainerElementConstraintsForGetterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "fishOfTheMonth" )
					.containerElementType( 1, 0 )
						.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareDeeplyNestedContainerElementConstraintsForGetterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "tagsOfFishOfTheMonth" )
					.containerElementType( 0, 1, 0 )
						.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementCascadesForGetterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "boss" )
					.containerElementType()
						.valid()
			.type( Fish.class )
				.getter( "name" )
					.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForArrayTypedGetterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "fishNames" )
					.containerElementType()
						.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForListContainingArrayTypeGetterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "fishNamesByMonth" )
					.containerElementType( 0, 0 )
						.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForMultiDimensionalArrayTypeGetterProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "fishNamesByMonthAsArray" )
					.containerElementType( 0, 0 )
						.constraint( new SizeDef().max( 5 ) );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
		);
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000211.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintOnNonGenericGetterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "size" )
					.containerElementType( 1 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingTypeArgumentIndexOnGetterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "model" )
					.containerElementType( 2 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingNestedTypeArgumentIndexOnGetterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "fishOfTheMonth" )
					.containerElementType( 1, 2 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000213.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnGetterCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "fishCountByType" )
					.containerElementType();
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000214.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void configuringSameContainerElementTwiceCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.getter( "tagsOfFishOfTheMonth" )
					.containerElementType( 0, 1, 0 )
						.constraint( new NotNullDef() )
					.containerElementType( 0, 1, 0 );
	}

	public static class FishTank {

		public Optional<String> getModel() {
			return Optional.of( "Too long" );
		}

		public Optional<Fish> getBoss() {
			return Optional.of( new Fish() );
		}

		public Map<String, Integer> getFishCountByType() {
			Map<String, Integer> fishCount = new HashMap<>();
			fishCount.put( "A", -1 );
			fishCount.put( "BB", -2 );
			return fishCount;
		}

		public Map<String, List<Fish>> getFishOfTheMonth() {
			Map<String, List<Fish>> fishOfTheMonth = new HashMap<>();

			List<Fish> january = Arrays.asList( null, new Fish() );
			fishOfTheMonth.put( "january", january );

			return fishOfTheMonth;
		}

		public List<Map<String, Set<String>>> getTagsOfFishOfTheMonth() {
			Set<String> bobsTags = CollectionHelper.asSet( (String) null );

			Map<String, Set<String>> january = new HashMap<>();
			january.put( "bob", bobsTags );

			List<Map<String, Set<String>>> tagsOfFishOfTheMonth = new ArrayList<>();
			tagsOfFishOfTheMonth.add( january );

			return tagsOfFishOfTheMonth;
		}

		public String[] getFishNames() {
			return new String[] { "Too Long" };
		}

		public List<String[]> getFishNamesByMonth() {
			List<String[]> names = new ArrayList<>();
			names.add(  new String[] { "Too Long" } );
			return names;
		}

		public String[][] getFishNamesByMonthAsArray() {
			String[][] names = new String[][]{ new String[] { "Too Long" } };
			return names;
		}

		public int getSize() {
			return 0;
		}
	}

	public static class Fish {

		public Fish() {
		}

		public String getName() {
			return null;
		}
	}
}
