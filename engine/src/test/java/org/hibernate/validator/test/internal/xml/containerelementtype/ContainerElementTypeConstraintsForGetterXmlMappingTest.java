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
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

public class ContainerElementTypeConstraintsForGetterXmlMappingTest {

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForGetterProgrammatically() {
		Validator validator = getValidator( "getter-canDeclareContainerElementTypeConstraints-mapping.xml" );

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
	public void canDeclareNestedContainerElementTypeConstraintsForGetterProgrammatically() {
		Validator validator = getValidator( "getter-canDeclareNestedContainerElementTypeConstraints-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"may not be null"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareDeeplyNestedContainerElementTypeConstraintsForGetterProgrammatically() {
		Validator validator = getValidator( "getter-canDeclareDeeplyNestedContainerElementTypeConstraints-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"may not be null"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementCascadesForGetterProgrammatically() {
		Validator validator = getValidator( "getter-canDeclareContainerElementCascades-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"may not be null"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForArrayTypeGetterProgrammatically() {
		Validator validator = getValidator( "getter-canDeclareContainerElementTypeConstraintsForArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 0 and 5"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForListContainingArrayTypeGetterProgrammatically() {
		Validator validator = getValidator( "getter-canDeclareContainerElementTypeConstraintsForListContainingArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 0 and 5"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayTypeGetterProgrammatically() {
		Validator validator = getValidator( "getter-canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 0 and 5"
		);
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000211.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintOnNonGenericFieldCausesException() {
		getValidator( "getter-declaringContainerElementTypeConstraintOnNonGenericFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnFieldCausesException() {
		getValidator( "getter-declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000212.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnFieldCausesException() {
		getValidator( "getter-declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000213.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnFieldCausesException() {
		getValidator( "getter-omittingTypeArgumentForMultiTypeArgumentTypeOnFieldCausesException-mapping.xml" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000217.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void configuringSameContainerElementTwiceCausesException() {
		getValidator( "getter-configuringSameContainerElementTwiceCausesException-mapping.xml" );
	}

	private Validator getValidator(String mappingFile) {
		Configuration<?> config = ValidatorUtil.getConfiguration();
		config.addMapping( ContainerElementTypeConstraintsForFieldXmlMappingTest.class.getResourceAsStream( mappingFile ) );
		return config.buildValidatorFactory().getValidator();
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
