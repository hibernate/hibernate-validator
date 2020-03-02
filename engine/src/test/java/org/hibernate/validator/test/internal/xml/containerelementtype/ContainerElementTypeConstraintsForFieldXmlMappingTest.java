/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.internal.xml.containerelementtype;

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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ContainerElementTypeConstraintsForFieldXmlMappingTest {

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForArrayTypeFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementTypeConstraintsForArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForListContainingArrayTypeFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementTypeConstraintsForListContainingArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayTypeFieldWithXmlMapping() {
		Validator validator = getValidator( "field-canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
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
		config.addMapping( getClass().getResourceAsStream( mappingFile ) );
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
