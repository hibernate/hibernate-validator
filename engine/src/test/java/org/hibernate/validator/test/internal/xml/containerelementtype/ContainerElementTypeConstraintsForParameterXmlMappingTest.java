/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.internal.xml.containerelementtype;

import static org.assertj.core.api.Assertions.fail;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolationException;
import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
@CandidateForTck
public class ContainerElementTypeConstraintsForParameterXmlMappingTest {

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForParameterWithXmlMapping() {
		Validator validator = getValidator( "parameter-canDeclareContainerElementTypeConstraints-mapping.xml" );

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
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareNestedContainerElementTypeConstraintsForParameterWithXmlMapping() {
		Validator validator = getValidator( "parameter-canDeclareNestedContainerElementTypeConstraints-mapping.xml" );

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
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareDeeplyNestedContainerElementTypeConstraintsForParameterWithXmlMapping() {
		Validator validator = getValidator( "parameter-canDeclareDeeplyNestedContainerElementTypeConstraints-mapping.xml" );

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
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementCascadesForParameterWithXmlMapping() {
		Validator validator = getValidator( "parameter-canDeclareContainerElementCascades-mapping.xml" );

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

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForArrayTypeParameterWithXmlMapping() {
		Validator validator = getValidator( "parameter-canDeclareContainerElementTypeConstraintsForArrayType-mapping.xml" );

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

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForListContainingArrayTypeParameterWithXmlMapping() {
		Validator validator = getValidator( "parameter-canDeclareContainerElementTypeConstraintsForListContainingArrayType-mapping.xml" );

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

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayTypeParameterWithXmlMapping() {
		Validator validator = getValidator( "parameter-canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayType-mapping.xml" );

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
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintOnNonGenericParameterCausesException() {
		getValidator( "parameter-declaringContainerElementTypeConstraintOnNonGenericParameterCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnParameterCausesException() {
		getValidator( "parameter-declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnParameterCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnParameterCausesException() {
		getValidator( "parameter-declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnParameterCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000213.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnParameterCausesException() {
		getValidator( "parameter-omittingTypeArgumentForMultiTypeArgumentTypeOnParameterCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030:.*")
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfListThrowsException() {
		Validator validator = getValidator( "parameter-configuringConstraintsOnGenericTypeArgumentOfListThrowsException-mapping.xml" );

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
		fishTank.test8( Arrays.asList( "Too long" ) );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030:.*")
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfArrayThrowsException() {
		Validator validator = getValidator( "parameter-configuringConstraintsOnGenericTypeArgumentOfArrayThrowsException-mapping.xml" );

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
		fishTank.test9( new String[]{ "Too long" } );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000217.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void configuringSameContainerElementTwiceCausesException() {
		getValidator( "parameter-configuringSameContainerElementTwiceCausesException-mapping.xml" );
	}

	private Validator getValidator(String mappingFile) {
		Configuration<?> config = ValidatorUtil.getConfiguration();
		config.addMapping( getClass().getResourceAsStream( mappingFile ) );
		return config.buildValidatorFactory().getValidator();
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
