/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.validation.ConstraintDefinitionException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.testng.annotations.Test;

import org.hibernate.validator.metadata.BeanMetaData;
import org.hibernate.validator.metadata.BeanMetaDataCache;
import org.hibernate.validator.metadata.BeanMetaDataImpl;
import org.hibernate.validator.metadata.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MethodMetaData;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepository;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryImpl;

import static org.hibernate.validator.test.util.TestUtil.assertSize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link BeanMetaData}.
 *
 * @author Gunnar Morling
 */
public class BeanMetaDataImplTest {

	@Test
	public void nonCascadingConstraintAtMethodReturnValue() throws Exception {

		BeanMetaData<CustomerRepository> metaData = setupBeanMetaData( CustomerRepository.class );

		Method method = CustomerRepository.class.getMethod( "baz" );
		MethodMetaData methodMetaData = metaData.getMetaDataForMethod( method ).get( CustomerRepository.class );

		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( methodMetaData.isCascading() );
		ConstraintDescriptorImpl<? extends Annotation> descriptor = methodMetaData.iterator()
				.next()
				.getDescriptor();
		assertEquals( descriptor.getAnnotation().annotationType(), Min.class );
		assertEquals( descriptor.getAttributes().get( "value" ), 10L );
	}

	@Test
	public void constraintFromBaseClass() throws Exception {

		BeanMetaData<CustomerRepositoryImpl> metaData = setupBeanMetaData( CustomerRepositoryImpl.class );

		Method method = CustomerRepository.class.getMethod( "baz" );
		MethodMetaData methodMetaData = metaData.getMetaDataForMethod( method ).get( CustomerRepository.class );

		assertSize( methodMetaData, 1 );
		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( methodMetaData.isCascading() );

		ConstraintDescriptorImpl<? extends Annotation> descriptor = methodMetaData.iterator()
				.next()
				.getDescriptor();
		assertEquals( descriptor.getAnnotation().annotationType(), Min.class );
		assertEquals( descriptor.getAttributes().get( "value" ), 10L );
	}

	@Test
	public void cascadingConstraintAtMethodReturnValue() throws Exception {

		BeanMetaData<CustomerRepository> metaData = setupBeanMetaData( CustomerRepository.class );

		Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
		MethodMetaData methodMetaData = metaData.getMetaDataForMethod( method ).get( CustomerRepository.class );

		assertEquals( methodMetaData.getMethod(), method );
		assertTrue( methodMetaData.isCascading() );
		assertSize( methodMetaData, 0 );
	}

	private <T> BeanMetaDataImpl<T> setupBeanMetaData(Class<T> clazz) {
		return new BeanMetaDataImpl<T>( clazz, new ConstraintHelper(), new BeanMetaDataCache() );
	}

	@Test(
			expectedExceptions = ConstraintDefinitionException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints\\. The following.*"
	)
	public void parameterConstraintsAddedInSubTypeCausesDefinitionException() {

		setupBeanMetaData( FooExtImpl.class );
	}

	@Test(
			expectedExceptions = ConstraintDefinitionException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints, but there are.*"
	)
	public void constraintStrengtheningInSubTypeCausesDefinitionException() {

		setupBeanMetaData( BarExtImpl.class );
	}

	@Test(
			expectedExceptions = ConstraintDefinitionException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints\\. The following.*"
	)
	public void parameterConstraintsInHierarchyWithMultipleRootMethodsCausesDefinitionException() {

		setupBeanMetaData( BazImpl.class );
	}

	public static interface Foo {

		void foo(String s);
	}

	public static interface FooExt extends Foo {

		/**
		 * Adds constraints to an un-constrained method from a super-type, which is not allowed.
		 */
		void foo(@NotNull String s);
	}

	public static class FooExtImpl implements FooExt {

		public void foo(String s) {
		}
	}

	public static interface Bar {

		void bar(@NotNull String s);
	}

	public static interface BarExt extends Bar {

		/**
		 * Adds constraints to a constrained method from a super-type, which is not allowed.
		 */
		void bar(@Size(min = 3) String s);
	}

	public static class BarExtImpl implements BarExt {

		public void bar(String s) {
		}
	}

	public static interface Baz1 {

		void baz(String s);
	}

	public static interface Baz2 {

		void baz(@Size(min = 3) String s);
	}

	public static class BazImpl implements Baz1, Baz2 {

		/**
		 * Implements a method defined by two interfaces, with di a super-type, which is not allowed.
		 */
		public void baz(String s) {
		}
	}
}
