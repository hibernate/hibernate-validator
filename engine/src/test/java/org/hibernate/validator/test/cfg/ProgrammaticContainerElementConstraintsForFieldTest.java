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
import org.hibernate.validator.cfg.defs.LengthDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class ProgrammaticContainerElementConstraintsForFieldTest {

	private HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "model" )
					.containerElementType()
						.constraint( new SizeDef().max( 5 ) )
				.field( "fishCountByType" )
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
	public void canDeclareNestedContainerElementConstraintsForFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "fishOfTheMonth" )
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
	public void canDeclareDeeplyNestedContainerElementConstraintsForFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "tagsOfFishOfTheMonth" )
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
	public void canDeclareContainerElementCascadesForFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "boss" )
					.containerElementType()
						.valid()
			.type( Fish.class )
				.field( "name" )
					.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1614")
	public void canDeclareDeeplyNestedContainerElementConstraintsOnMultipleDifferentTypeArgumentsForFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( FishTank.class )
				.field( "tagsOfFishOfTheMonth" )
					.containerElementType( 0, 0 )
						.constraint( new LengthDef().min( 10 ).max( 20 ) )
					.containerElementType( 0, 1, 0 )
						.constraint( new NotNullDef() );

		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" ),
				violationOf( Length.class ).withMessage( "length must be between 10 and 20" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void canDeclareContainerElementConstraintsForArrayTypedFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "fishNames" )
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
	public void canDeclareContainerElementConstraintsForListContainingArrayTypeFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "fishNamesByMonth" )
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
	public void canDeclareContainerElementConstraintsForMultiDimensionalArrayTypeFieldProgrammatically() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "fishNamesByMonthAsArray" )
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
	public void declaringContainerElementConstraintOnNonGenericFieldCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "size" )
					.containerElementType( 1 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingTypeArgumentIndexOnFieldCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "model" )
					.containerElementType( 2 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void declaringContainerElementConstraintForNonExistingNestedTypeArgumentIndexOnFieldCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "fishOfTheMonth" )
					.containerElementType( 1, 2 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000213.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnFieldCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "fishCountByType" )
					.containerElementType();
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000214.*")
	@TestForIssue(jiraKey = "HV-1239")
	public void configuringSameContainerElementTwiceCausesException() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
			.type( FishTank.class )
				.field( "tagsOfFishOfTheMonth" )
					.containerElementType( 0, 1, 0 )
						.constraint( new NotNullDef() )
					.containerElementType( 0, 1, 0 );
	}

	public static class FishTank {

		public Optional<String> model = Optional.of( "Too long" );
		public Optional<Fish> boss = Optional.of( new Fish() );
		public Map<String, Integer> fishCountByType;
		public Map<String, List<Fish>> fishOfTheMonth;
		public List<Map<String, Set<String>>> tagsOfFishOfTheMonth;
		public String[] fishNames = new String[] { "Too Long" };
		public List<String[]> fishNamesByMonth;
		public String[][] fishNamesByMonthAsArray = new String[][]{ new String[] { "Too Long" } };
		public int size = 0;

		public FishTank() {
			fishCountByType = new HashMap<>();
			fishCountByType.put( "A", -1 );
			fishCountByType.put( "BB", -2 );

			fishOfTheMonth = new HashMap<>();
			List<Fish> january = Arrays.asList( null, new Fish() );
			fishOfTheMonth.put( "january", january );

			Set<String> bobsTags = CollectionHelper.asSet( (String) null );
			Map<String, Set<String>> januaryTags = new HashMap<>();
			januaryTags.put( "bob", bobsTags );
			tagsOfFishOfTheMonth = new ArrayList<>();
			tagsOfFishOfTheMonth.add( januaryTags );

			fishNamesByMonth = new ArrayList<>();
			fishNamesByMonth.add(  new String[] { "Too Long" } );
		}
	}

	public static class Fish {

		public Fish() {
		}

		public String name;
	}
}
