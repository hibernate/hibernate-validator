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

import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.method.metadata.TypeDescriptor;
import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.util.Contracts.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link TypeDescriptor} and its creation.
 *
 * @author Gunnar Morling
 */
public class TypeDescriptorTest {

	@Test
	public void testGetElementClass() {

		TypeDescriptor descriptor = getTypeDescriptor( CustomerRepository.class );
		assertEquals( descriptor.getElementClass(), CustomerRepository.class );
	}

	@Test
	public void testIsTypeConstrainedForUnconstrainedType() {

		TypeDescriptor descriptor = getTypeDescriptor( UnconstrainedType.class );

		assertFalse( descriptor.isTypeConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForBeanConstrainedType() {

		TypeDescriptor descriptor = getTypeDescriptor( CustomerRepository.class );

		assertTrue( descriptor.isTypeConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForParameterConstrainedType() {

		TypeDescriptor descriptor = getTypeDescriptor( ParameterConstrainedType.class );

		assertTrue( descriptor.isTypeConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForReturnValueConstrainedType() {

		TypeDescriptor descriptor = getTypeDescriptor( ReturnValueConstrainedType.class );

		assertTrue( descriptor.isTypeConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForCascadingParameterType() {

		TypeDescriptor descriptor = getTypeDescriptor( CascadingParameterType.class );

		assertTrue( descriptor.isTypeConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForCascadingReturnValueType() {

		TypeDescriptor descriptor = getTypeDescriptor( CascadingReturnValueType.class );

		assertTrue( descriptor.isTypeConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForDerivedConstrainedType() {

		TypeDescriptor descriptor = getTypeDescriptor( DerivedConstrainedType.class );

		assertTrue( descriptor.isTypeConstrained() );
	}

	@Test
	public void testGetConstraintDescriptors() {

		TypeDescriptor descriptor = getTypeDescriptor( CustomerRepository.class );
		Set<ConstraintDescriptor<?>> constraintDescriptors = descriptor.getConstraintDescriptors();

		assertEquals( constraintDescriptors.size(), 1 );
		assertEquals( constraintDescriptors.iterator().next().getAnnotation().annotationType(), ScriptAssert.class );
	}

	@Test
	public void testGetBeanDescriptor() {

		TypeDescriptor descriptor = getTypeDescriptor( CustomerRepository.class );
		BeanDescriptor beanDescriptor = descriptor.getBeanDescriptor();

		assertNotNull( beanDescriptor );
		assertEquals( beanDescriptor.getElementClass(), CustomerRepository.class );
	}

	private TypeDescriptor getTypeDescriptor(Class<?> clazz) {
		return TestUtil.getMethodValidator().getConstraintsForType( clazz );
	}

	private static class UnconstrainedType {

		@SuppressWarnings("unused")
		public void foo(String foo) {

		}
	}

	private static class ParameterConstrainedType {

		@SuppressWarnings("unused")
		public void foo(@NotNull String foo) {

		}
	}

	private static class CascadingParameterType {

		@SuppressWarnings("unused")
		public void foo(@Valid List<String> foo) {

		}
	}

	private static class ReturnValueConstrainedType {

		@NotNull
		@SuppressWarnings("unused")
		public String foo(String foo) {
			return null;
		}
	}

	private static class CascadingReturnValueType {

		@Valid
		@SuppressWarnings("unused")
		public List<String> foo(String foo) {
			return null;
		}
	}

	private static class DerivedConstrainedType extends ParameterConstrainedType {

		public void foo(String foo) {

		}
	}

}
