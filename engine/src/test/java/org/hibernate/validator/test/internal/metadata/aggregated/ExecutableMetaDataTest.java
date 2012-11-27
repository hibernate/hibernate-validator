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
package org.hibernate.validator.test.internal.metadata.aggregated;

import java.lang.reflect.Method;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.joda.time.DateMidnight;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.test.internal.metadata.ConsistentDateParameters;
import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository.ValidationGroup;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests creation of {@link ExecutableMetaData} in {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl}.
 *
 * @author Gunnar Morling
 */
public class ExecutableMetaDataTest {

	private BeanMetaData<CustomerRepositoryExt> beanMetaData;

	@BeforeMethod
	public void setupBeanMetaData() {

		beanMetaData = new BeanMetaDataManager( new ConstraintHelper() ).getBeanMetaData( CustomerRepositoryExt.class );
	}

	@Test
	public void methodWithConstrainedParameter() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		List<ParameterMetaData> parameterMetaData = methodMetaData.getAllParameterMetaData();
		assertEquals( parameterMetaData.size(), 2 );

		assertFalse( parameterMetaData.get( 0 ).isConstrained() );
		assertFalse( parameterMetaData.get( 0 ).isCascading() );

		assertTrue( parameterMetaData.get( 1 ).isConstrained() );
		assertFalse( parameterMetaData.get( 1 ).isCascading() );
		assertThat( parameterMetaData.get( 1 ) ).hasSize( 1 );
		assertEquals(
				parameterMetaData.get( 1 ).iterator().next().getDescriptor().getAnnotation().annotationType(),
				NotNull.class
		);

		assertEquals( parameterMetaData.get( 0 ), methodMetaData.getParameterMetaData( 0 ) );
		assertEquals( parameterMetaData.get( 1 ), methodMetaData.getParameterMetaData( 1 ) );

		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void methodWithCascadedParameter() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "saveCustomer", Customer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		List<ParameterMetaData> parameterMetaData = methodMetaData.getAllParameterMetaData();
		assertEquals( parameterMetaData.size(), 1 );

		assertTrue( parameterMetaData.get( 0 ).isConstrained() );
		assertTrue( parameterMetaData.get( 0 ).isCascading() );
		assertThat( parameterMetaData.get( 0 ) ).isEmpty();

		assertEquals( parameterMetaData.get( 0 ), methodMetaData.getParameterMetaData( 0 ) );

		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void methodWithCrossParameterConstraint() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod(
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		);
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		assertThat( methodMetaData.getCrossParameterConstraints() ).hasSize( 1 );
		assertThat(
				methodMetaData.getCrossParameterConstraints()
						.iterator()
						.next()
						.getDescriptor()
						.getAnnotation()
						.annotationType()
		).isEqualTo( ConsistentDateParameters.class );
	}

	@Test
	public void methodWithConstrainedReturnValue() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "bar" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).hasSize( 1 );
		assertEquals(
				methodMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);

		assertThat( methodMetaData.getAllParameterMetaData() ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void returnValueConstraintsAddUpInHierarchy() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "baz" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).hasSize( 2 );
	}

	@Test
	public void methodWithCascadedReturnValue() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertTrue( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void locallyDefinedGroupConversion() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "methodWithReturnValueGroupConversion" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertThat(
				methodMetaData.getReturnValueMetaData()
						.getCascadables()
						.iterator()
						.next()
						.convertGroup( Default.class )
		).isEqualTo( ValidationGroup.class );
	}

	@Test
	public void unconstrainedMethod() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "updateCustomer", Customer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertEquals( methodMetaData.getParameterTypes(), method.getParameterTypes() );
		assertFalse( methodMetaData.isCascading() );
		assertFalse( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}
}
