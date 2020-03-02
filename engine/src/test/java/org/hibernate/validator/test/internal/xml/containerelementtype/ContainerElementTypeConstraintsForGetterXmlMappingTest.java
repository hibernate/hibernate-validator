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

public class ContainerElementTypeConstraintsForGetterXmlMappingTest {

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForArrayTypeGetterWithXmlMapping() {
		Validator validator = getValidator( "getter-canDeclareContainerElementTypeConstraintsForArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForListContainingArrayTypeGetterWithXmlMapping() {
		Validator validator = getValidator( "getter-canDeclareContainerElementTypeConstraintsForListContainingArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000226:.*")
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayTypeGetterWithXmlMapping() {
		Validator validator = getValidator( "getter-canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayType-mapping.xml" );

		Set<ConstraintViolation<FishTank>> violations = validator.validate( new FishTank() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 0 and 5" )
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
		config.addMapping( getClass().getResourceAsStream( mappingFile ) );
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
