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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.joda.time.DateMidnight;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.IllegalCustomerRepositoryExt;
import org.hibernate.validator.testutil.TestForIssue;

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
public class BeanDescriptorTest {

	@Test
	public void testGetElementClass() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		assertEquals( descriptor.getElementClass(), CustomerRepository.class );
	}

	@Test
	public void testIsTypeConstrainedForUnconstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( UnconstrainedType.class );
		assertFalse( descriptor.isBeanConstrained() );
		assertFalse( descriptor.hasConstrainedExecutables() );
	}

	@Test
	public void testIsBeanConstrainedClassLevelConstraint() {
		BeanDescriptor descriptor = getBeanDescriptor( ClassLevelConstrainedType.class );
		assertTrue( descriptor.isBeanConstrained() );
		assertFalse( descriptor.hasConstrainedExecutables() );
	}

	@Test
	public void testIsBeanConstrainedFieldConstraint() {
		BeanDescriptor descriptor = getBeanDescriptor( FieldConstrainedType.class );
		assertTrue( descriptor.isBeanConstrained() );
		assertFalse( descriptor.hasConstrainedExecutables() );
	}

	@Test
	public void testIsBeanConstrainedGetterConstraint() {
		BeanDescriptor descriptor = getBeanDescriptor( GetterConstrainedType.class );
		assertTrue( descriptor.isBeanConstrained() );
		assertFalse( descriptor.hasConstrainedExecutables() );
	}

	@Test
	public void testIsTypeConstrainedForBeanConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		assertTrue( descriptor.isBeanConstrained() );
		assertTrue( descriptor.hasConstrainedExecutables() );
	}

	@Test
	public void testIsTypeConstrainedForParameterConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( ParameterConstrainedType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForConstructorParameterConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( ConstructorParameterConstrainedType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForCascadingParameterType() {
		BeanDescriptor descriptor = getBeanDescriptor( CascadingParameterType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForConstructorCascadingParameterType() {
		BeanDescriptor descriptor = getBeanDescriptor( ConstructorCascadingParameterType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForReturnValueConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( ReturnValueConstrainedType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForConstructorReturnValueConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( ConstructorReturnValueConstrainedType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForCascadingReturnValueType() {
		BeanDescriptor descriptor = getBeanDescriptor( CascadingReturnValueType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForConstructorCascadingReturnValueType() {
		BeanDescriptor descriptor = getBeanDescriptor( ConstructorCascadingReturnValueType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testIsTypeConstrainedForDerivedConstrainedType() {
		BeanDescriptor descriptor = getBeanDescriptor( DerivedConstrainedType.class );
		assertFalse( descriptor.isBeanConstrained(), "The entity should have no bean constraints" );
		assertTrue( descriptor.hasConstrainedExecutables(), "The entity should have constraint methods/constructors " );
	}

	@Test
	public void testGetConstraintDescriptors() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepository.class );
		Set<ConstraintDescriptor<?>> constraintDescriptors = descriptor.getConstraintDescriptors();

		assertEquals( constraintDescriptors.size(), 1 );
		assertEquals(
				constraintDescriptors.iterator().next().getAnnotation().annotationType(),
				ScriptAssert.class
		);
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

	@Test
	public void testGetConstraintsForMethodSucceedsAlsoIfTypeContainsAnotherIllegalMethod() throws Exception {
		BeanDescriptor descriptor = getBeanDescriptor( IllegalCustomerRepositoryExt.class );
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
		MethodDescriptor methodDescriptor = descriptor.getConstraintsForMethod(
				"qax",
				Integer.class
		);

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
				"createCustomer",
				"saveCustomer",
				"foo",
				"bar",
				"baz",
				"zap",
				"qax",
				"methodWithCrossParameterConstraint",
				"methodWithParameterGroupConversion",
				"methodWithReturnValueGroupConversion"
		);
	}

	@Test
	public void testGetConstrainedMethodsForDerivedType() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		Set<MethodDescriptor> constrainedMethods = descriptor.getConstrainedMethods();

		assertThat( getMethodNames( constrainedMethods ) ).containsOnly(
				"createCustomer",
				"saveCustomer",
				"modifyCustomer",
				"foo",
				"bar",
				"baz",
				"zip",
				"zap",
				"qax",
				"methodWithCrossParameterConstraint",
				"methodWithParameterGroupConversion",
				"methodWithReturnValueGroupConversion"
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	@TestForIssue(jiraKey = "HV-683")
	public void testGetConstrainedMethodsForTypeWithIllegalMethodCausesDeclarationException() {
		getBeanDescriptor( IllegalCustomerRepositoryExt.class ).getConstrainedMethods();
	}

	@Test
	public void testGetConstrainedConstructors() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		Set<ConstructorDescriptor> constrainedConstructors = descriptor.getConstrainedConstructors();

		assertThat( constrainedConstructors ).isNotNull();
		assertThat( getSignatures( constrainedConstructors ) ).containsOnly(
				Arrays.<Class<?>>asList( String.class ),
				Arrays.<Class<?>>asList( String.class, Customer.class ),
				Collections.emptyList(),
				Arrays.<Class<?>>asList( DateMidnight.class, DateMidnight.class )
		);
	}

	@Test
	public void testGetConstraintsForConstructor() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		ConstructorDescriptor constructorDescriptor = descriptor.getConstraintsForConstructor(
				String.class,
				Customer.class
		);

		assertThat( constructorDescriptor ).isNotNull();
	}

	@Test
	public void testGetConstraintsForUnconstrainedConstructor() {
		BeanDescriptor descriptor = getBeanDescriptor( CustomerRepositoryExt.class );
		ConstructorDescriptor constructorDescriptor = descriptor.getConstraintsForConstructor( int.class );

		assertThat( constructorDescriptor ).isNull();
	}

	@Test
	@TestForIssue(jiraKey = "HV-660")
	public void testGetConstrainedPropertiesForTypeWithClassLevelConstraint() {
		BeanDescriptor descriptor = getBeanDescriptor( ClassLevelConstrainedType.class );
		Set<PropertyDescriptor> constrainedProperties = descriptor.getConstrainedProperties();

		assertThat( constrainedProperties ).isEmpty();
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

	private static class ConstructorParameterConstrainedType {
		@SuppressWarnings("unused")
		public ConstructorParameterConstrainedType(@NotNull String foo) {
		}
	}

	private static class CascadingParameterType {
		@SuppressWarnings("unused")
		public void foo(@Valid List<String> foo) {
		}
	}

	private static class ConstructorCascadingParameterType {
		@SuppressWarnings("unused")
		public ConstructorCascadingParameterType(@Valid List<String> foo) {
		}
	}

	private static class ReturnValueConstrainedType {
		@NotNull
		@SuppressWarnings("unused")
		public String foo(String foo) {
			return null;
		}
	}

	private static class ConstructorReturnValueConstrainedType {
		@NotNull
		@SuppressWarnings("unused")
		public ConstructorReturnValueConstrainedType(String foo) {
		}
	}

	private static class CascadingReturnValueType {
		@Valid
		@SuppressWarnings("unused")
		public List<String> foo(String foo) {
			return null;
		}
	}

	private static class ConstructorCascadingReturnValueType {
		@Valid
		@SuppressWarnings("unused")
		public ConstructorCascadingReturnValueType(String foo) {
		}
	}

	private static class DerivedConstrainedType extends ParameterConstrainedType {
		@Override
		public void foo(String foo) {
		}
	}

	@ScriptAssert(lang = "", script = "")
	private static class ClassLevelConstrainedType {
	}

	private static class FieldConstrainedType {
		@NotNull
		private String foo;
	}

	private static class GetterConstrainedType {
		private String foo;

		@NotNull
		public String getFoo() {
			return foo;
		}
	}
}
