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
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.metadata.AggregatedMethodMetaData;
import org.hibernate.validator.metadata.BeanMetaDataCache;
import org.hibernate.validator.metadata.BeanMetaDataImpl;
import org.hibernate.validator.metadata.ConstraintHelper;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests creation of {@link AggregatedMethodMetaData} in {@link BeanMetaDataImpl}.
 *
 * @author Gunnar Morling
 */
public class AggregatedMethodMetaDataTest {

	private BeanMetaDataImpl<CustomerRepositoryExt> beanMetaData;

	@BeforeMethod
	public void setupBeanMetaData() {

		beanMetaData = new BeanMetaDataImpl<CustomerRepositoryExt>(
				CustomerRepositoryExt.class, new ConstraintHelper(), new BeanMetaDataCache()
		);
	}

	@Test
	public void methodWithConstrainedParameter() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", String.class, String.class );
		AggregatedMethodMetaData methodMetaData = beanMetaData.getAggregatedMethodMetaData( method );

		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertSize( methodMetaData, 0 );
	}

	@Test
	public void methodWithCascadedParameter() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "saveCustomer", Customer.class );
		AggregatedMethodMetaData methodMetaData = beanMetaData.getAggregatedMethodMetaData( method );

		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertSize( methodMetaData, 0 );
	}

	@Test
	public void methodWithConstrainedReturnValue() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "bar" );
		AggregatedMethodMetaData methodMetaData = beanMetaData.getAggregatedMethodMetaData( method );

		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertSize( methodMetaData, 1 );
		assertEquals(
				methodMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);
	}

	@Test
	public void returnValueConstraintsAddUpInHierarchy() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "baz" );
		AggregatedMethodMetaData methodMetaData = beanMetaData.getAggregatedMethodMetaData( method );

		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertSize( methodMetaData, 2 );
	}

	@Test
	public void methodWithCascadedReturnValue() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		AggregatedMethodMetaData methodMetaData = beanMetaData.getAggregatedMethodMetaData( method );

		assertEquals( methodMetaData.getMethod(), method );
		assertTrue( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertSize( methodMetaData, 0 );
	}

	@Test
	public void unconstrainedMethod() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "updateCustomer", Customer.class );
		AggregatedMethodMetaData methodMetaData = beanMetaData.getAggregatedMethodMetaData( method );

		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( methodMetaData.isCascading() );
		assertFalse( methodMetaData.isConstrained() );
		assertSize( methodMetaData, 0 );
	}

	/**
	 * TODO GM: invoke on TestUtil, once it is merged.
	 */
	@Deprecated
	private void assertSize(Iterable<?> iterable, int expectedCount) {

		int i = 0;

		for ( @SuppressWarnings("unused") Object o : iterable ) {
			i++;
		}

		assertEquals( i, expectedCount, "Actual size of iterable [" + iterable + "] differed from expected size." );
	}
}
