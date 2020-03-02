/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup2;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationSequence;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroupImpl;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration test for the group sequence processing during method-level validation.
 *
 * @author Gunnar Morling
 */
@Test
public class MethodLevelValidationGroupSequenceTest {

	private CustomerRepositoryWithRedefinedDefaultGroup customerRepository;

	@BeforeMethod
	public void setUpDefaultExecutableValidator() {
		setUpValidatorForGroups();
	}

	private void setUpValidatorForGroups(Class<?>... groups) {
		customerRepository = getValidatingProxy(
				new CustomerRepositoryWithRedefinedDefaultGroupImpl(), ValidatorUtil.getValidator(), groups
		);
	}

	@Test
	public void validationSucceedsAsNoConstraintInDefaultSequenceIsViolated() {
		customerRepository.noConstraintInDefaultGroup( null );
	}

	@Test
	public void validationFailsAsConstraintInDefaultSequenceIsViolated() {

		try {
			customerRepository.constraintInDefaultGroup( null );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withInvalidValue( null )
							.withRootBeanClass( CustomerRepositoryWithRedefinedDefaultGroupImpl.class )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
			);
		}
	}

	/**
	 * Only one constraint violation is expected, as processing should stop after the
	 * first erroneous group of the default sequence.
	 */
	@Test
	public void processingOfDefaultSequenceStopsAfterFirstErroneousGroup() {

		try {
			customerRepository.constraintInLaterPartOfDefaultSequence( 1 );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class )
							.withMessage( "must be greater than or equal to 5" )
							.withInvalidValue( 1 )
							.withRootBeanClass( CustomerRepositoryWithRedefinedDefaultGroupImpl.class )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
			);
		}
	}

	/**
	 * Two constraint violations (originating from different parameters) from ValidationGroup1 expected.
	 * Third violation from ValidationGroup2 is not expected, as sequence processing stopped after first group.
	 */
	@Test
	public void processingOfDefaultSequenceStopsAfterFirstErroneousGroupWithSeveralParameters() {

		try {
			customerRepository.constraintInLaterPartOfDefaultSequenceAtDifferentParameters( 1, 2 );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class ).withMessage( "must be greater than or equal to 5" ),
					violationOf( Min.class ).withMessage( "must be greater than or equal to 7" )
			);
		}
	}

	/**
	 * Only one constraint violation is expected, as processing should stop after the
	 * first erroneous group of the validated sequence.
	 */
	@Test
	public void processingOfGroupSequenceStopsAfterFirstErroneousGroup() {

		setUpValidatorForGroups( ValidationSequence.class );

		try {
			customerRepository.constraintInLaterPartOfGroupSequence( 1 );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class )
							.withMessage( "must be greater than or equal to 5" )
							.withInvalidValue( 1 )
							.withRootBeanClass( CustomerRepositoryWithRedefinedDefaultGroupImpl.class )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup2.class
			);
		}
	}

	/**
	 * Two constraint violations (originating from different parameters) from ValidationGroup2 expected.
	 * Third violation from ValidationGroup3 is not expected, as sequence processing stopped after first group.
	 */
	@Test
	public void processingOfGroupSequenceStopsAfterFirstErroneousGroupWithSeveralParameters() {

		setUpValidatorForGroups( ValidationSequence.class );

		try {
			customerRepository.constraintInLaterPartOfGroupSequenceAtDifferentParameters( 1, 2 );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {

			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class ).withMessage( "must be greater than or equal to 5" ),
					violationOf( Min.class ).withMessage( "must be greater than or equal to 7" )
			);
		}
	}

	// Tests for return value validation below
	// TODO GM: extract to separate test

	@Test
	public void validationSucceedsAsNoConstraintInDefaultSequenceAtReturnValueIsViolated() {
		customerRepository.noConstraintInDefaultGroupAtReturnValue();
	}

	@Test
	public void validationFailsAsConstraintInDefaultSequenceAtReturnValueIsViolated() {

		try {
			customerRepository.constraintInDefaultGroupAtReturnValue();
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withMessage( "must not be null" )
							.withInvalidValue( null )
							.withRootBeanClass( CustomerRepositoryWithRedefinedDefaultGroupImpl.class )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
			);
		}
	}

	/**
	 * Only one constraint violation is expected, as processing should stop after the
	 * first erroneous group of the default sequence.
	 */
	@Test
	public void processingOfDefaultSequenceForReturnValueStopsAfterFirstErroneousGroup() {

		try {
			customerRepository.constraintsInAllPartOfDefaultSequence();
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class )
							.withMessage( "must be greater than or equal to 5" )
							.withInvalidValue( 1 )
							.withRootBeanClass( CustomerRepositoryWithRedefinedDefaultGroupImpl.class )
			);
			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
			);
		}
	}

	/**
	 * Only one constraint violation is expected, as processing should stop after the
	 * first erroneous group of the validated sequence.
	 */
	@Test
	public void processingOfGroupSequenceForReturnValueStopsAfterFirstErroneousGroup() {

		setUpValidatorForGroups( ValidationSequence.class );

		try {
			customerRepository.constraintsInAllPartsOfGroupSequence();
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class )
							.withMessage( "must be greater than or equal to 5" )
							.withInvalidValue( 1 )
							.withRootBeanClass( CustomerRepositoryWithRedefinedDefaultGroupImpl.class )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup2.class
			);
		}
	}
}
