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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.internal.metadata.descriptor.BeanDescriptorImpl;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.testutil.ValidatorUtil.getBeanDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link BeanDescriptor} and its creation.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
@Test
public class BeanDescriptorTest {

	@Test
	public void testGetElementClass() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		assertEquals( descriptor.getElementClass(), CustomerRepository.class );
	}

	@Test
	public void testElementDescriptorType() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		assertEquals( descriptor.getKind(), ElementDescriptor.Kind.BEAN );
	}

	@Test
	public void testNarrowDescriptor() {
		ElementDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );

		BeanDescriptor beanDescriptor = descriptor.as( BeanDescriptor.class );
		assertTrue( beanDescriptor != null );

		beanDescriptor = descriptor.as( BeanDescriptorImpl.class );
		assertTrue( beanDescriptor != null );
	}

	@Test(expectedExceptions = ClassCastException.class, expectedExceptionsMessageRegExp = "HV000118.*")
	public void testUnableToNarrowDescriptor() {
		ElementDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		descriptor.as( MethodDescriptor.class );
	}

	@Test
	public void testIsTypeConstrainedForUnconstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( UnconstrainedType.class );
		assertFalse( descriptor.isBeanConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForBeanConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		assertTrue( descriptor.isBeanConstrained() );
	}

	@Test
	public void testIsTypeConstrainedForParameterConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( ParameterConstrainedType.class );
		assertTrue( descriptor.isBeanConstrained(), "The entity should be constrained" );
	}

	@Test
	public void testIsTypeConstrainedForReturnValueConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( ReturnValueConstrainedType.class );
		assertTrue( descriptor.isBeanConstrained(), "The entity should be constrained" );
	}

	@Test
	public void testIsTypeConstrainedForCascadingParameterType() {
		BeanDescriptor descriptor = getBeanDescriptor( CascadingParameterType.class );
		assertTrue( descriptor.isBeanConstrained(), "The entity should be constrained" );
	}

	@Test
	public void testIsTypeConstrainedForCascadingReturnValueType() {
		BeanDescriptor descriptor = getBeanDescriptor( CascadingReturnValueType.class );
		assertTrue( descriptor.isBeanConstrained(), "The entity should be constrained" );
	}

	@Test
	public void testIsTypeConstrainedForDerivedConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( DerivedConstrainedType.class );
		assertTrue( descriptor.isBeanConstrained(), "The entity should be constrained" );
	}

	@Test
	public void testGetConstraintDescriptors() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		Set<ConstraintDescriptor<?>> constraintDescriptors = descriptor.getConstraintDescriptors();

		assertEquals( constraintDescriptors.size(), 1 );
		assertEquals( constraintDescriptors.iterator().next().getAnnotation().annotationType(), ScriptAssert.class );
	}

	@Test
	public void testGetBeanDescriptor() {
		BeanDescriptor beanDescriptor = getBeanDescriptor( CustomerRepository.class );

		assertNotNull( beanDescriptor );
		assertEquals( beanDescriptor.getElementClass(), CustomerRepository.class );
	}

	@Test
	public void testGetConstraintsForMethod() throws Exception {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		MethodDescriptor methodDescriptor = descriptor.getConstraintsForMethod( "foo" );

		assertNotNull( methodDescriptor );
	}

	// A method descriptor can be retrieved by specifying an overridden method
	// from a base type.
	@Test
	public void testGetConstraintsForOverriddenMethod() throws Exception {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		MethodDescriptor methodDescriptor = descriptor.getConstraintsForMethod( "foo" );

		assertNotNull( methodDescriptor );
	}

	// A method descriptor can be retrieved by specifying a method from a base
	// type (qax() is not defined on CustomerRepositoryExt, but only on
	// CustomerRepository).
	@Test
	public void testGetConstraintsForMethodFromBaseType() throws Exception {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		MethodDescriptor methodDescriptor = descriptor.getConstraintsForMethod( "qax", Integer.class );

		assertNotNull( methodDescriptor );
	}

	@Test
	public void testGetConstraintsForUnknownMethod() throws Exception {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		assertNull( descriptor.getConstraintsForMethod( "zap" ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGetConstraintsFailsForNullMethod() throws Exception {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		descriptor.getConstraintsForMethod( null );
	}

	@Test
	public void testGetConstrainedMethods() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		Set<MethodDescriptor> constrainedMethods = descriptor.getConstrainedMethods();

		assertThat( getMethodNames( constrainedMethods ) ).containsOnly(
				"createCustomer", "saveCustomer", "foo", "bar", "baz", "zap", "qax"
		);
	}

	@Test
	public void testGetConstrainedMethodsForDerivedType() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		Set<MethodDescriptor> constrainedMethods = descriptor.getConstrainedMethods();

		assertThat( getMethodNames( constrainedMethods ) ).containsOnly(
				"createCustomer", "saveCustomer", "foo", "bar", "baz", "zip", "zap", "qax"
		);
	}

	@Test
	public void testGetConstrainedConstructors() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		Set<ConstructorDescriptor> constrainedConstructors = descriptor.getConstrainedConstructors();

		assertThat( constrainedConstructors ).isNotNull();
		assertThat( getSignatures( constrainedConstructors ) ).containsOnly(
				Arrays.<Class<?>>asList( String.class ),
				Arrays.<Class<?>>asList( String.class, int.class )
		);
	}

	private Set<String> getMethodNames(Set<MethodDescriptor> descriptors) {
		Set<String> methodNames = newHashSet();

		for ( MethodDescriptor methodDescriptor : descriptors ) {
			methodNames.add( methodDescriptor.getName() );
		}

		return methodNames;
	}

	private Set<List<Class<?>>> getSignatures(Set<ConstructorDescriptor> descriptors) {
		Set<List<Class<?>>> signatures = newHashSet();

		for ( ConstructorDescriptor methodDescriptor : descriptors ) {
			List<Class<?>> parameterTypes = newArrayList();

			for ( ParameterDescriptor oneParameter : methodDescriptor.getParameterDescriptors() ) {
				parameterTypes.add( oneParameter.getElementClass() );
			}
			signatures.add( parameterTypes );
		}

		return signatures;
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
