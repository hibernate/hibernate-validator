/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.testutils.ValidatorUtil.getMethodDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.ParameterDescriptor;
import jakarta.validation.metadata.Scope;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.CustomerExtension;
import org.hibernate.validator.test.internal.metadata.IllegalCustomerRepositoryExt;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @author Gunnar Morling
 */
public class MethodDescriptorTest {

	@Test
	public void testGetMethod() throws Exception {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"foo"
		);
		assertEquals( "foo", methodDescriptor.getName() );
	}

	@Test
	public void testIsCascaded() {
		MethodDescriptor cascadingMethodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"foo"
		);
		assertTrue( cascadingMethodDescriptor.getReturnValueDescriptor().isCascaded() );

		MethodDescriptor nonCascadingMethodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"baz"
		);
		assertFalse( nonCascadingMethodDescriptor.getReturnValueDescriptor().isCascaded() );
	}

	@Test
	public void testHasConstraints() {
		MethodDescriptor descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"bar"
		);
		assertFalse(
				descriptor.hasConstraints(),
				"Method has no constraints."
		);

		descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"methodWithCrossParameterConstraint",
				LocalDate.class,
				LocalDate.class
		);
		assertFalse(
				descriptor.hasConstraints(),
				"Cross-parameter constraints shouldn't be reported on MethodDescriptor."
		);
	}

	@Test
	public void testGetElementClass() {
		//the return type as defined in the base type
		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepository.class, "bar" );
		assertEquals( Customer.class, methodDescriptor.getElementClass() );

		//the return type is now the one as defined in the derived type (covariant return type)
		methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "bar" );
		assertEquals( CustomerExtension.class, methodDescriptor.getElementClass() );
	}

	@Test
	public void testGetConstraintDescriptors() {
		MethodDescriptor descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"bar"
		);
		assertTrue( descriptor.getConstraintDescriptors().isEmpty() );

		descriptor = getMethodDescriptor(
				CustomerRepository.class,
				"methodWithCrossParameterConstraint",
				LocalDate.class,
				LocalDate.class
		);
		assertTrue( descriptor.getConstraintDescriptors().isEmpty() );
	}

	@Test
	public void testFindConstraintsMatchingGroups() {
		MethodDescriptor descriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"methodWithCrossParameterConstraint",
				LocalDate.class,
				LocalDate.class
		);

		assertTrue(
				descriptor.findConstraints()
						.getConstraintDescriptors()
						.isEmpty()
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-443")
	public void testFindParameterConstraintLookingAt() {
		ParameterDescriptor parameterDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"createCustomer",
				CharSequence.class,
				String.class
		).getParameterDescriptors().get( 1 );

		Set<ConstraintDescriptor<?>> constraintDescriptors = parameterDescriptor.findConstraints()
				.lookingAt( Scope.LOCAL_ELEMENT )
				.getConstraintDescriptors();
		assertEquals( 0, constraintDescriptors.size() );

		constraintDescriptors = parameterDescriptor.findConstraints()
				.lookingAt( Scope.HIERARCHY )
				.getConstraintDescriptors();
		assertEquals( 1, constraintDescriptors.size() );
		assertEquals(
				NotNull.class,
				constraintDescriptors.iterator().next().getAnnotation().annotationType() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-683")
	public void testGetMethodDescriptorForIllegalyConfiguredMethodCausesConstraintDeclarationException() {
		assertThatThrownBy( () -> getMethodDescriptor( IllegalCustomerRepositoryExt.class, "zap", int.class ) )
				.isInstanceOf( ConstraintDeclarationException.class )
				.hasMessageMatching( "HV000151.*" );
	}

	@Test
	public void testGetParameterConstraints() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class, "createCustomer", CharSequence.class, String.class
		);

		List<ParameterDescriptor> parameterConstraints = methodDescriptor.getParameterDescriptors();
		assertNotNull( parameterConstraints );
		assertEquals( 2, parameterConstraints.size() );

		ParameterDescriptor parameterDescriptor1 = parameterConstraints.get( 0 );
		assertEquals( CharSequence.class, parameterDescriptor1.getElementClass() );
		assertFalse( parameterDescriptor1.hasConstraints() );

		ParameterDescriptor parameterDescriptor2 = parameterConstraints.get( 1 );
		assertEquals( String.class, parameterDescriptor2.getElementClass() );
		assertTrue( parameterDescriptor2.hasConstraints() );
	}

	@Test
	public void testGetParameterConstraintsForParameterlessMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"baz"
		);

		List<ParameterDescriptor> parameterConstraints = methodDescriptor.getParameterDescriptors();
		assertNotNull( parameterConstraints );
		assertEquals( 0, parameterConstraints.size() );
	}

	@Test
	public void testGetReturnValueDescriptorForVoidMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"saveCustomer",
				Customer.class
		);
		assertThat( methodDescriptor.getReturnValueDescriptor() ).isNotNull();
	}

	@Test
	public void testIsReturnValueConstrainedForConstrainedMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"baz"
		);
		assertThat( methodDescriptor.hasConstrainedReturnValue() ).isTrue();
	}

	@Test
	public void testIsReturnValueConstrainedForCascadedMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"foo"
		);
		assertThat( methodDescriptor.hasConstrainedReturnValue() ).isTrue();
	}

	@Test
	public void testIsReturnValueConstrainedForParameterConstrainedMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"createCustomer",
				CharSequence.class,
				String.class
		);
		assertThat( methodDescriptor.hasConstrainedReturnValue() ).isFalse();
	}

	@Test
	public void testIsReturnValueConstrainedForVoidMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"saveCustomer",
				Customer.class
		);
		assertThat( methodDescriptor.hasConstrainedReturnValue() ).isFalse();
	}

	@Test
	public void testAreParametersConstrainedForParameterConstrainedMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"createCustomer",
				CharSequence.class,
				String.class
		);
		assertThat( methodDescriptor.hasConstrainedParameters() ).isTrue();
	}

	@Test
	public void testAreParametersConstrainedForParameterCascadedMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"saveCustomer",
				Customer.class
		);
		assertThat( methodDescriptor.hasConstrainedParameters() ).isTrue();
	}

	@Test
	public void testAreParametersConstrainedForCrossParameterConstrainedMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"methodWithCrossParameterConstraint", LocalDate.class, LocalDate.class
		);
		assertThat( methodDescriptor.hasConstrainedParameters() ).isTrue();
	}

	@Test
	public void testAreParametersConstrainedForNonParameterConstrainedMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"zip",
				int.class
		);
		assertThat( methodDescriptor.hasConstrainedParameters() ).isFalse();
	}

	@Test
	public void testAreParametersConstrainedForParameterlessMethod() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"zip"
		);
		assertThat( methodDescriptor.hasConstrainedParameters() ).isFalse();
	}
}
