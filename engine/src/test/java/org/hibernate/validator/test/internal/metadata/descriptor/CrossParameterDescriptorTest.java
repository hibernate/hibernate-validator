/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.metadata.descriptor;

import java.util.Set;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.CrossParameterDescriptor;
import javax.validation.metadata.Scope;

import org.joda.time.DateMidnight;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.ConsistentDateParameters;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepository.ValidationGroup;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintTypes;
import static org.hibernate.validator.testutil.ValidatorUtil.getConstructorDescriptor;
import static org.hibernate.validator.testutil.ValidatorUtil.getMethodDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Gunnar Morling
 */
public class CrossParameterDescriptorTest {

	@Test
	public void testGetElementClass() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();

		assertEquals(
				descriptor.getElementClass(),
				Object[].class
		);
	}

	@Test
	public void testGetConstraintDescriptorsForMethod() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertConstraintTypes(
				descriptor.getConstraintDescriptors(),
				ConsistentDateParameters.class
		);
	}

	@Test
	public void testGetConstraintDescriptorsForConstructor() {
		CrossParameterDescriptor descriptor = getConstructorDescriptor(
				CustomerRepository.class,
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertConstraintTypes(
				descriptor.getConstraintDescriptors(),
				ConsistentDateParameters.class
		);
	}

	@Test
	public void testGetConstraintDescriptorsForMethodConsidersConstraintsFromSuperType() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertConstraintTypes(
				descriptor.getConstraintDescriptors(),
				ConsistentDateParameters.class
		);
	}

	@Test
	public void testGetConstraintDescriptorsForConstructorDoesNotConsiderConstraintsFromSuperType() {
		CrossParameterDescriptor descriptor = getConstructorDescriptor(
				CustomerRepositoryExt.class,
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertTrue(
				descriptor.getConstraintDescriptors().isEmpty()
		);
	}

	@Test
	public void testHasConstraintsForMethod() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"bar"
		).getCrossParameterDescriptor();
		assertFalse(
				descriptor.hasConstraints(),
				"Method has no cross-parameter constraints."
		);

		descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertTrue(
				descriptor.hasConstraints(),
				"Method has one cross-parameter constraint."
		);
	}

	@Test
	public void testHasConstraintsForConstructor() {
		CrossParameterDescriptor descriptor = getConstructorDescriptor(
				CustomerRepository.class
		).getCrossParameterDescriptor();
		assertFalse(
				descriptor.hasConstraints(),
				"Constructor has no cross-parameter constraints."
		);

		descriptor = getConstructorDescriptor(
				CustomerRepository.class,
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertTrue(
				descriptor.hasConstraints(),
				"Constructor has one cross-parameter constraint."
		);
	}

	@Test
	public void testHasConstraintsForMethodConsidersConstraintsFromSuperType() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertTrue(
				descriptor.hasConstraints(),
				"Method has one cross-parameter constraint defined in supertype."
		);
	}

	@Test
	public void testHasConstraintsForConstructorDoesNotConsiderConstraintsFromSuperType() {
		CrossParameterDescriptor descriptor = getConstructorDescriptor(
				CustomerRepositoryExt.class,
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();
		assertFalse(
				descriptor.hasConstraints(),
				"Constructor has no cross-parameter."
		);
	}

	@Test
	public void testFindConstraintsMatchingGroups() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();

		assertTrue(
				descriptor.findConstraints()
						.unorderedAndMatchingGroups( Default.class )
						.getConstraintDescriptors()
						.isEmpty()
		);
		assertConstraintTypes(
				descriptor.findConstraints()
						.unorderedAndMatchingGroups( ValidationGroup.class )
						.getConstraintDescriptors(), ConsistentDateParameters.class
		);
	}

	@Test
	public void testFindConstraintsLookingAt() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		).getCrossParameterDescriptor();

		Set<ConstraintDescriptor<?>> constraintDescriptors = descriptor.findConstraints()
				.lookingAt( Scope.LOCAL_ELEMENT )
				.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 0 );

		constraintDescriptors = descriptor.findConstraints()
				.lookingAt( Scope.HIERARCHY )
				.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 1 );
		assertEquals(
				constraintDescriptors.iterator().next().getAnnotation().annotationType(),
				ConsistentDateParameters.class
		);
	}

	@Test
	public void testCrossParameterDescriptorForMethodWithoutCrossParameterConstraints() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"zip",
				int.class
		).getCrossParameterDescriptor();

		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.hasConstraints() ).isFalse();
		assertThat( descriptor.getConstraintDescriptors() ).isEmpty();
		assertThat( descriptor.findConstraints().getConstraintDescriptors() ).isEmpty();
	}
}
