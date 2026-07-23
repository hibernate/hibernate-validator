/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.cascadedgroupvalidation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Jan-Willem Willebrands
 */
@Test
@TestForIssue(jiraKey = "HV-1072")
public class CascadedMethodLevelValidationGroupSequenceTest {
	private CompoundEntityRepository entityRepository;

	private void setUpValidatorForGroups(Class<?>... groups) {
		entityRepository = getValidatingProxy(
				new CompoundEntityRepositoryImpl(), ValidatorUtil.getValidator(), groups
		);
	}

	/**
	 * Expect a single constraint violation from the first violation group.
	 */
	@Test
	private void cascadedConstraintViolationInFirstGroupOnly() {
		setUpValidatorForGroups( CompoundGroup.class );
		assertThatThrownBy( () -> entityRepository.store( new CompoundEntity( new Entity( null, "value" ) ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException ex = (ConstraintViolationException) e;
					assertThat( ex.getConstraintViolations() ).containsOnlyViolations(
							violationOf( NotNull.class )
									.withMessage( "must not be null" )
									.withInvalidValue( null )
									.withRootBeanClass( CompoundEntityRepositoryImpl.class )
					);

					ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
					assertEquals(
							constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
					);
				} );
	}

	/**
	 * Expect a single constraint violation from the second validation group.
	 */
	@Test
	private void cascadedConstraintViolationInSecondGroupOnly() {
		setUpValidatorForGroups( CompoundGroup.class );
		assertThatThrownBy( () -> entityRepository.store( new CompoundEntity( new Entity( "value", null ) ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException ex = (ConstraintViolationException) e;
					assertThat( ex.getConstraintViolations() ).containsOnlyViolations(
							violationOf( NotNull.class )
									.withMessage( "must not be null" )
									.withInvalidValue( null )
									.withRootBeanClass( CompoundEntityRepositoryImpl.class )
					);

					ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
					assertEquals(
							constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup2.class
					);
				} );
	}

	/**
	 * Expect a single constraint violation in the first group. The second group should not be
	 * validated due to the violation in the first group.
	 */
	@Test
	private void cascadedConstraintViolationInBothGroups() {
		setUpValidatorForGroups( CompoundGroup.class );
		assertThatThrownBy( () -> entityRepository.store( new CompoundEntity( new Entity( null, null ) ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException ex = (ConstraintViolationException) e;
					assertThat( ex.getConstraintViolations() ).containsOnlyViolations(
							violationOf( NotNull.class )
									.withMessage( "must not be null" )
									.withInvalidValue( null )
									.withRootBeanClass( CompoundEntityRepositoryImpl.class )
					);

					ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
					assertEquals(
							constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
					);
				} );
	}

	/**
	 * Expect a single constraint violation from the first validation group.
	 */
	@Test
	private void cascadedReturnValueConstraintInFirstGroup() {
		setUpValidatorForGroups( CompoundGroup.class );
		assertThatThrownBy( () -> entityRepository.getEntity( new CompoundEntity( new Entity( null, "value" ) ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException ex = (ConstraintViolationException) e;
					assertThat( ex.getConstraintViolations() ).containsOnlyViolations(
							violationOf( NotNull.class )
									.withMessage( "must not be null" )
									.withInvalidValue( null )
									.withRootBeanClass( CompoundEntityRepositoryImpl.class )
					);
					ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
					assertEquals(
							constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
					);
				} );
	}

	/**
	 * Expect a single constraint violation from the second validation group.
	 */
	@Test
	private void cascadedReturnValueConstraintInSecondGroup() {
		setUpValidatorForGroups( CompoundGroup.class );
		assertThatThrownBy( () -> entityRepository.getEntity( new CompoundEntity( new Entity( "value", null ) ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException ex = (ConstraintViolationException) e;
					assertThat( ex.getConstraintViolations() ).containsOnlyViolations(
							violationOf( NotNull.class )
									.withMessage( "must not be null" )
									.withInvalidValue( null )
									.withRootBeanClass( CompoundEntityRepositoryImpl.class )
					);

					ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
					assertEquals(
							constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup2.class
					);
				} );
	}

	/**
	 * Expect a single constraint violation in the first group. The second group should not be
	 * validated due to the violation in the first group.
	 */
	@Test
	private void cascadedReturnValueConstraintInBothGroups() {
		setUpValidatorForGroups( CompoundGroup.class );
		assertThatThrownBy( () -> entityRepository.getEntity( new CompoundEntity( new Entity( null, null ) ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException ex = (ConstraintViolationException) e;
					assertThat( ex.getConstraintViolations() ).containsOnlyViolations(
							violationOf( NotNull.class )
									.withMessage( "must not be null" )
									.withInvalidValue( null )
									.withRootBeanClass( CompoundEntityRepositoryImpl.class )
					);
					ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
					assertEquals(
							constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
					);
				} );
	}
}
