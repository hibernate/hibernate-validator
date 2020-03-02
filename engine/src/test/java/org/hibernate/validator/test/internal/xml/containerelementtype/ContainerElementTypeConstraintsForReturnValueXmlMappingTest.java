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

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;

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
public class ContainerElementTypeConstraintsForReturnValueXmlMappingTest {

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForArrayTypeReturnValueWithXmlMapping() {
		Validator validator = getValidator( "returnvalue-canDeclareContainerElementTypeConstraintsForArrayType-mapping.xml" );

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			fishTank.test5();

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
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForListContainingArrayTypeReturnValueWithXmlMapping() {
		Validator validator = getValidator( "returnvalue-canDeclareContainerElementTypeConstraintsForListContainingArrayType-mapping.xml" );

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			fishTank.test6();

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
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayTypeReturnValueWithXmlMapping() {
		Validator validator = getValidator( "returnvalue-canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayType-mapping.xml" );

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

		try {
			fishTank.test7();

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
	public void declaringContainerElementTypeConstraintOnNonGenericReturnValueCausesException() {
		getValidator( "returnvalue-declaringContainerElementTypeConstraintOnNonGenericReturnValueCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnReturnValueCausesException() {
		getValidator( "returnvalue-declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnReturnValueCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnReturnValueCausesException() {
		getValidator( "returnvalue-declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnReturnValueCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000213.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnReturnValueCausesException() {
		getValidator( "returnvalue-omittingTypeArgumentForMultiTypeArgumentTypeOnReturnValueCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030:.*")
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfListThrowsException() {
		Validator validator = getValidator( "returnvalue-configuringConstraintsOnGenericTypeArgumentOfListThrowsException-mapping.xml" );

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
		fishTank.test8( Arrays.asList( "Too long" ) );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	//@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030:.*")
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfArrayThrowsException() {
		Validator validator = getValidator( "returnvalue-configuringConstraintsOnGenericTypeArgumentOfArrayThrowsException-mapping.xml" );

		IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
		fishTank.test9( new String[]{ "Too long" } );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000217.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void configuringSameContainerElementTwiceCausesException() {
		getValidator( "returnvalue-configuringSameContainerElementTwiceCausesException-mapping.xml" );
	}

	private Validator getValidator(String mappingFile) {
		Configuration<?> config = ValidatorUtil.getConfiguration();
		config.addMapping( getClass().getResourceAsStream( mappingFile ) );
		return config.buildValidatorFactory().getValidator();
	}

	public interface IFishTank {
		Map<String, Integer> test1();
		Map<String, List<Fish>> test2();
		List<Map<String, Set<String>>> test3();
		Optional<Fish> test4();
		String[] test5();
		List<String[]> test6();
		String[][]  test7();
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
			fishNamesByMonth.add(  new String[] { "Too Long" } );
			return fishNamesByMonth;
		}

		@Override
		public String[][] test7() {
			return new String[][]{ new String[] { "Too Long" } };
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
