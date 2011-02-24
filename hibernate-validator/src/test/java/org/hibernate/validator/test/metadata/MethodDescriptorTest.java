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
package org.hibernate.validator.test.metadata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.Scope;

import org.testng.annotations.Test;

import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.ParameterDescriptor;
import org.hibernate.validator.method.metadata.TypeDescriptor;
import org.hibernate.validator.test.metadata.CustomerRepository.ValidationGroup;
import org.hibernate.validator.test.metadata.CustomerRepositoryExt.CustomerExtension;
import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.test.util.TestUtil.getMethodDescriptor;
import static org.hibernate.validator.util.Contracts.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Gunnar Morling
 */
public class MethodDescriptorTest {

	@Test
	public void testGetMethod() throws Exception {

		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "foo" );
		assertEquals( methodDescriptor.getMethod(), CustomerRepositoryExt.class.getMethod( "foo" ) );
	}

	/**
	 * The descriptor is retrieved using foo() from the base type, but it
	 * references foo() from the type represented by the type descriptor.
	 */
	@Test
	public void testGetMethodForOverriddenMethod() throws Exception {

		TypeDescriptor typeDescriptor = TestUtil.getMethodValidator().getConstraintsForType( CustomerRepositoryExt.class );
		assertNotNull( typeDescriptor );
		
		Method methodFromBaseType = CustomerRepository.class.getMethod( "foo" );
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		MethodDescriptor methodDescriptor = typeDescriptor.getConstraintsForMethod( methodFromBaseType );

		assertNotNull( methodDescriptor );
		assertEquals( methodDescriptor.getMethod(), method );
	}

	@Test
	public void testIsCascaded() {

		MethodDescriptor cascadingMethodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "foo" );
		assertTrue( cascadingMethodDescriptor.isCascaded() );

		MethodDescriptor nonCascadingMethodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "baz" );
		assertFalse( nonCascadingMethodDescriptor.isCascaded() );
	}

	@Test
	public void testHasConstraints() {

		MethodDescriptor constrainedMethodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "bar" );
		assertTrue( constrainedMethodDescriptor.hasConstraints() );

		MethodDescriptor unconstrainedMethodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "qux" );
		assertFalse( unconstrainedMethodDescriptor.hasConstraints() );
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

		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "bar" );

		assertEquals( methodDescriptor.getConstraintDescriptors().size(), 1 );
		assertEquals(
				methodDescriptor.getConstraintDescriptors().iterator().next().getAnnotation().annotationType(),
				NotNull.class
		);

		methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "baz" );
		assertEquals( methodDescriptor.getConstraintDescriptors().size(), 2 );
	}

	@Test
	public void testFindConstraintLookingAt() {

		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "baz" );

		Set<ConstraintDescriptor<?>> constraintDescriptors = methodDescriptor.findConstraints()
				.lookingAt( Scope.LOCAL_ELEMENT )
				.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 1 );
		assertEquals( constraintDescriptors.iterator().next().getAnnotation().annotationType(), Min.class );

		constraintDescriptors = methodDescriptor.findConstraints()
				.lookingAt( Scope.HIERARCHY )
				.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 2 );
	}

	@Test
	public void testFindConstraintMatchingGroups() {

		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "baz" );

		Set<ConstraintDescriptor<?>> constraintDescriptors = methodDescriptor.findConstraints()
				.unorderedAndMatchingGroups( ValidationGroup.class )
				.getConstraintDescriptors();

		assertEquals( constraintDescriptors.size(), 1 );
		assertEquals( constraintDescriptors.iterator().next().getAnnotation().annotationType(), NotNull.class );
	}

	@Test
	public void testGetParameterConstraints() {

		MethodDescriptor methodDescriptor = getMethodDescriptor(
				CustomerRepositoryExt.class, "createCustomer", String.class, String.class
		);

		List<ParameterDescriptor> parameterConstraints = methodDescriptor.getParameterConstraints();
		assertNotNull( parameterConstraints );
		assertEquals( parameterConstraints.size(), 2 );
	}

	@Test
	public void testGetParameterConstraintsForParameterlessMethod() {

		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepositoryExt.class, "baz" );

		List<ParameterDescriptor> parameterConstraints = methodDescriptor.getParameterConstraints();
		assertNotNull( parameterConstraints );
		assertEquals( parameterConstraints.size(), 0 );
	}
}
