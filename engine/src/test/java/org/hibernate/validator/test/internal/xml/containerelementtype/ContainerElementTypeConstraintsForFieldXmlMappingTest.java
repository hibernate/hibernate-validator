/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.internal.xml.containerelementtype;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

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
public class ContainerElementTypeConstraintsForFieldXmlMappingTest {

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementTypeConstraints-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 0 and 5",
				"size must be between 3 and 10",
				"size must be between 3 and 10",
				"must be greater than or equal to 1",
				"must be greater than or equal to 1"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareNestedContainerElementTypeConstraintsForFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareNestedContainerElementTypeConstraints-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"may not be null"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareDeeplyNestedContainerElementTypeConstraintsForFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareDeeplyNestedContainerElementTypeConstraints-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"may not be null"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementCascadesForFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementCascades-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"may not be null"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForArrayTypeFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementTypeConstraintsForArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 0 and 5"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForListContainingArrayTypeFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementTypeConstraintsForListContainingArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 0 and 5"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayTypeFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 0 and 5"
		);
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000211.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintOnNonGenericFieldCausesException() {
		getValidator( "field-declaringContainerElementTypeConstraintOnNonGenericFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnFieldCausesException() {
		getValidator( "field-declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnFieldCausesException() {
		getValidator( "field-declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000213.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnFieldCausesException() {
		getValidator( "field-omittingTypeArgumentForMultiTypeArgumentTypeOnFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000217.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void configuringSameContainerElementTwiceCausesException() {
		getValidator( "field-configuringSameContainerElementTwiceCausesException-mapping.xml" );
	}

	private Validator getValidator(String mappingFile) {
		Configuration<?> config = ValidatorUtil.getConfiguration();
		config.addMapping( ContainerElementTypeConstraintsForFieldXmlMappingTest.class.getResourceAsStream( mappingFile ) );
		return config.buildValidatorFactory().getValidator();
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
