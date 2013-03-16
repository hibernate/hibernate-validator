/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.Scope;

import org.joda.time.DateMidnight;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.CustomerExtension;
import org.hibernate.validator.test.internal.metadata.IllegalCustomerRepositoryExt;
import org.hibernate.validator.testutil.TestForIssue;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.testutil.ValidatorUtil.getMethodDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
		assertEquals( methodDescriptor.getName(), "foo" );
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
				DateMidnight.class,
				DateMidnight.class
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
		assertEquals( methodDescriptor.getElementClass(), Customer.class );

		//the return type is now the one as defined in the derived type (covariant return type)
		methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "bar" );
		assertEquals( methodDescriptor.getElementClass(), CustomerExtension.class );
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
				DateMidnight.class,
				DateMidnight.class
		);
		assertTrue( descriptor.getConstraintDescriptors().isEmpty() );
	}

	@Test
	public void testFindConstraintsMatchingGroups() {
		MethodDescriptor descriptor = getMethodDescriptor(
				CustomerRepositoryExt.class,
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
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
		assertEquals( constraintDescriptors.size(), 0 );

		constraintDescriptors = parameterDescriptor.findConstraints()
				.lookingAt( Scope.HIERARCHY )
				.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 1 );
		assertEquals(
				constraintDescriptors.iterator().next().getAnnotation().annotationType(),
				NotNull.class
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	@TestForIssue(jiraKey = "HV-683")
	public void testGetMethodDescriptorForIllegalyConfiguredMethodCausesConstraintDeclarationException() {
		getMethodDescriptor( IllegalCustomerRepositoryExt.class, "zap", int.class );
	}

	@Test
	public void testGetParameterConstraints() {
		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class, "createCustomer", CharSequence.class, String.class
		);

		List<ParameterDescriptor> parameterConstraints = methodDescriptor.getParameterDescriptors();
		assertNotNull( parameterConstraints );
		assertEquals( parameterConstraints.size(), 2 );

		ParameterDescriptor parameterDescriptor1 = parameterConstraints.get( 0 );
		assertEquals( parameterDescriptor1.getElementClass(), CharSequence.class );
		assertFalse( parameterDescriptor1.hasConstraints() );

		ParameterDescriptor parameterDescriptor2 = parameterConstraints.get( 1 );
		assertEquals( parameterDescriptor2.getElementClass(), String.class );
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
		assertEquals( parameterConstraints.size(), 0 );
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
				"methodWithCrossParameterConstraint", DateMidnight.class, DateMidnight.class
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
