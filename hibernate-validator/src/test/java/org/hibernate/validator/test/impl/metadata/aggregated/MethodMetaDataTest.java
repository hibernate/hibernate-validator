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
package org.hibernate.validator.test.impl.metadata.aggregated;

import java.lang.reflect.Method;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.impl.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.impl.metadata.BeanMetaDataManager;
import org.hibernate.validator.impl.metadata.core.ConstraintHelper;
import org.hibernate.validator.impl.metadata.aggregated.MethodMetaData;
import org.hibernate.validator.impl.metadata.raw.ConstrainedMethod;
import org.hibernate.validator.test.impl.metadata.Customer;
import org.hibernate.validator.test.impl.metadata.CustomerRepository;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertIterableSize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests creation of {@link ConstrainedMethod} in {@link org.hibernate.validator.impl.metadata.aggregated.BeanMetaDataImpl}.
 *
 * @author Gunnar Morling
 */
public class MethodMetaDataTest {

	private BeanMetaData<CustomerRepository> beanMetaData;

	@BeforeMethod
	public void setupBeanMetaData() {

		beanMetaData = new BeanMetaDataManager( new ConstraintHelper() ).getBeanMetaData( CustomerRepository.class );
	}

	@Test
	public void methodWithConstrainedParameter() throws Exception {

		Method method = CustomerRepository.class.getMethod( "createCustomer", CharSequence.class, String.class );
		MethodMetaData methodMetaData = beanMetaData.getMetaDataFor( method );

		assertEquals( methodMetaData.getName(), method.getName() );
		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertIterableSize( methodMetaData, 0 );
		assertEquals( methodMetaData.getAllParameterMetaData().size(), 2 );
	}

	@Test
	public void methodWithCascadedParameter() throws Exception {

		Method method = CustomerRepository.class.getMethod( "saveCustomer", Customer.class );
		MethodMetaData methodMetaData = beanMetaData.getMetaDataFor( method );

		assertEquals( methodMetaData.getName(), method.getName() );
		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertIterableSize( methodMetaData, 0 );
		assertEquals( methodMetaData.getAllParameterMetaData().size(), 1 );
	}

	@Test
	public void methodWithConstrainedReturnValue() throws Exception {

		Method method = CustomerRepository.class.getMethod( "bar" );
		MethodMetaData methodMetaData = beanMetaData.getMetaDataFor( method );

		assertEquals( methodMetaData.getName(), method.getName() );
		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertIterableSize( methodMetaData, 1 );
		assertEquals(
				methodMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);
		assertEquals( methodMetaData.getAllParameterMetaData().size(), 0 );
	}

	@Test
	public void methodWithCascadedReturnValue() throws Exception {

		Method method = CustomerRepository.class.getMethod( "foo" );
		MethodMetaData methodMetaData = beanMetaData.getMetaDataFor( method );

		assertEquals( methodMetaData.getName(), method.getName() );
		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertTrue( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertIterableSize( methodMetaData, 0 );
		assertEquals( methodMetaData.getAllParameterMetaData().size(), 0 );
	}

	@Test
	public void unconstrainedMethod() throws Exception {

		Method method = CustomerRepository.class.getMethod( "updateCustomer", Customer.class );
		MethodMetaData methodMetaData = beanMetaData.getMetaDataFor( method );

		assertEquals( methodMetaData.getName(), method.getName() );
		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertFalse( methodMetaData.isConstrained() );
		assertIterableSize( methodMetaData, 0 );
		assertEquals( methodMetaData.getAllParameterMetaData().size(), 1 );
	}

}
