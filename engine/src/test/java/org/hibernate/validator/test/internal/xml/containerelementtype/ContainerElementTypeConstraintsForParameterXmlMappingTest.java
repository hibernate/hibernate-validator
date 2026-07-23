/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.test.internal.xml.containerelementtype;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ContainerElementTypeConstraintsForParameterXmlMappingTest {

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForArrayTypeParameterWithXmlMapping() {
		assertThatThrownBy( () -> {
			Validator validator = getValidator( "parameter-canDeclareContainerElementTypeConstraintsForArrayType-mapping.xml" );

			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

			fishTank.test5( new String[] { "Too Long" } );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForListContainingArrayTypeParameterWithXmlMapping() {
		assertThatThrownBy( () -> {
			Validator validator = getValidator( "parameter-canDeclareContainerElementTypeConstraintsForListContainingArrayType-mapping.xml" );

			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

			List<String[]> fishNamesByMonth = new ArrayList<>();
			fishNamesByMonth.add( new String[] { "Too Long" } );

			fishTank.test6( fishNamesByMonth );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayTypeParameterWithXmlMapping() {
		assertThatThrownBy( () -> {
			Validator validator = getValidator( "parameter-canDeclareContainerElementTypeConstraintsForMultiDimensionalArrayType-mapping.xml" );

			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );

			String[][] fishNamesByMonthAsArray = new String[][] { new String[] { "Too Long" } };

			fishTank.test7( fishNamesByMonthAsArray );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintOnNonGenericParameterCausesException() {
		assertThatThrownBy( () -> getValidator( "parameter-declaringContainerElementTypeConstraintOnNonGenericParameterCausesException-mapping.xml" ) )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000211.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnParameterCausesException() {
		assertThatThrownBy( () -> getValidator( "parameter-declaringContainerElementTypeConstraintForNonExistingTypeArgumentIndexOnParameterCausesException-mapping.xml" ) )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000212.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnParameterCausesException() {
		assertThatThrownBy( () -> getValidator( "parameter-declaringContainerElementTypeConstraintForNonExistingNestedTypeArgumentIndexOnParameterCausesException-mapping.xml" ) )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000212.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void omittingTypeArgumentForMultiTypeArgumentTypeOnParameterCausesException() {
		assertThatThrownBy( () -> getValidator( "parameter-omittingTypeArgumentForMultiTypeArgumentTypeOnParameterCausesException-mapping.xml" ) )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000213.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1279")
	public void configuringConstraintsOnGenericTypeArgumentOfListThrowsException() {
		assertThatThrownBy( () -> {
			Validator validator = getValidator( "parameter-configuringConstraintsOnGenericTypeArgumentOfListThrowsException-mapping.xml" );

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
			Validator validator = getValidator( "parameter-configuringConstraintsOnGenericTypeArgumentOfArrayThrowsException-mapping.xml" );

			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
			fishTank.test9( new String[] { "Too long" } );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000226:.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1291")
	public void configuringSameContainerElementTwiceCausesException() {
		assertThatThrownBy( () -> getValidator( "parameter-configuringSameContainerElementTwiceCausesException-mapping.xml" ) )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000217.*" );
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
