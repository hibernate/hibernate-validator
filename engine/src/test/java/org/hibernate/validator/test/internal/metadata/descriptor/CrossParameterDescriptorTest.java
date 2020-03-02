/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintTypes;
import static org.hibernate.validator.testutils.ValidatorUtil.getConstructorDescriptor;
import static org.hibernate.validator.testutils.ValidatorUtil.getMethodDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Set;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.CrossParameterDescriptor;
import jakarta.validation.metadata.Scope;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.ConsistentDateParameters;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepository.ValidationGroup;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;

/**
 * @author Gunnar Morling
 */
public class CrossParameterDescriptorTest {

	@Test
	public void testGetElementClass() {
		CrossParameterDescriptor descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"methodWithCrossParameterConstraint",
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
				LocalDate.class,
				LocalDate.class
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
