/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;
import org.testng.annotations.Test;

@CandidateForTck
public class GenericModelLegacyCascadingTest {

	@Test
	@TestForIssue(jiraKey = "HV-1481")
	public void testCascadingOnObject() {
		Validator validator = getValidator();

		GenericModelHolder<InvalidModel> holder = new GenericModelHolder<>();
		holder.setModel( new InvalidModel() );

		Set<ConstraintViolation<GenericModelHolder<?>>> constraintViolations = validator.validate( holder );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "model" )
								.property( "notNullProperty" ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1481")
	public void testCascadingOnList() {
		Validator validator = getValidator();

		GenericModelHolder<List<InvalidModel>> holder = new GenericModelHolder<>();
		holder.setModel( Arrays.asList( new InvalidModel() ) );

		Set<ConstraintViolation<GenericModelHolder<?>>> constraintViolations = validator.validate( holder );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "model" )
								.property( "notNullProperty", true, null, 0, List.class, 0 ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1481")
	public void testCascadingOnMap() {
		Validator validator = getValidator();

		Map<String, InvalidModel> map = new HashMap<>();
		map.put( "invalidModel", new InvalidModel() );

		GenericModelHolder<Map<String, InvalidModel>> holder = new GenericModelHolder<>();
		holder.setModel( map );

		Set<ConstraintViolation<GenericModelHolder<?>>> constraintViolations = validator.validate( holder );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "model" )
								.property( "notNullProperty", true, "invalidModel", null, Map.class, 1 ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1481")
	public void testCascadingOnIterable() {
		Validator validator = getValidator();

		GenericModelHolder<Set<InvalidModel>> holder = new GenericModelHolder<>();
		holder.setModel( CollectionHelper.asSet( new InvalidModel() ) );

		Set<ConstraintViolation<GenericModelHolder<?>>> constraintViolations = validator.validate( holder );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "model" )
								.property( "notNullProperty", true, null, null, Iterable.class, 0 ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1481")
	public void testCascadingOnArray() {
		Validator validator = getValidator();

		GenericModelHolder<InvalidModel[]> holder = new GenericModelHolder<>();
		holder.setModel( new InvalidModel[]{ new InvalidModel() } );

		Set<ConstraintViolation<GenericModelHolder<?>>> constraintViolations = validator.validate( holder );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "model" )
								.property( "notNullProperty", true, null, 0, Object[].class, null ) ) );
	}

	private class GenericModelHolder<M> {

		private M model;

		@Valid
		public M getModel() {
			return model;
		}

		public void setModel(M model) {
			this.model = model;
		}
	}

	public class InvalidModel {

		@NotNull
		public Object getNotNullProperty() {
			return null;
		}
	}
}
