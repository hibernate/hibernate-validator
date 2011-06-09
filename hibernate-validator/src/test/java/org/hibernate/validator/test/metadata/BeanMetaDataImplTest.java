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
import javax.validation.constraints.Min;

import org.testng.annotations.Test;

import org.hibernate.validator.metadata.AggregatedMethodMetaData;
import org.hibernate.validator.metadata.BeanMetaData;
import org.hibernate.validator.metadata.BeanMetaDataCache;
import org.hibernate.validator.metadata.BeanMetaDataImpl;
import org.hibernate.validator.metadata.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepository;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryImpl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertIterableSize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link BeanMetaData}.
 *
 * TODO GM: Check, whether these tests are still needed. They seem redundant to
 * {@link MethodMetaDataTest} and {@link AggregatedMethodMetaDataTest}.
 *
 * @author Gunnar Morling
 */
public class BeanMetaDataImplTest {

	@Test
	public void nonCascadingConstraintAtMethodReturnValue() throws Exception {

		BeanMetaData<CustomerRepository> metaData = setupBeanMetaData( CustomerRepository.class );

		Method method = CustomerRepository.class.getMethod( "baz" );
		AggregatedMethodMetaData methodMetaData = metaData.getMetaDataFor( method );

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

		Method method = CustomerRepositoryImpl.class.getMethod( "baz" );
		AggregatedMethodMetaData methodMetaData = metaData.getMetaDataFor( method );

		assertIterableSize( methodMetaData, 1 );
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
		AggregatedMethodMetaData methodMetaData = metaData.getMetaDataFor( method );

		assertEquals( methodMetaData.getMethod(), method );
		assertTrue( methodMetaData.isCascading() );
		assertIterableSize( methodMetaData, 0 );
	}

	private <T> BeanMetaDataImpl<T> setupBeanMetaData(Class<T> clazz) {
		return new BeanMetaDataImpl<T>( clazz, new ConstraintHelper(), new BeanMetaDataCache() );
	}

}
