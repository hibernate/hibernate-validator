/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup2;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationSequence;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroupImpl;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation, "may not be null", CustomerRepositoryWithRedefinedDefaultGroupImpl.class, null
			);
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
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation,
					"must be greater than or equal to 5",
					CustomerRepositoryWithRedefinedDefaultGroupImpl.class,
					1
			);
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
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e.getConstraintViolations(),
					"must be greater than or equal to 5",
					"must be greater than or equal to 7"
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
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation,
					"must be greater than or equal to 5",
					CustomerRepositoryWithRedefinedDefaultGroupImpl.class,
					1
			);
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
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e.getConstraintViolations(),
					"must be greater than or equal to 5",
					"must be greater than or equal to 7"
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
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation, "may not be null", CustomerRepositoryWithRedefinedDefaultGroupImpl.class, null
			);
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
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation,
					"must be greater than or equal to 5",
					CustomerRepositoryWithRedefinedDefaultGroupImpl.class,
					1
			);
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
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation,
					"must be greater than or equal to 5",
					CustomerRepositoryWithRedefinedDefaultGroupImpl.class,
					1
			);
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup2.class
			);
		}
	}
}
