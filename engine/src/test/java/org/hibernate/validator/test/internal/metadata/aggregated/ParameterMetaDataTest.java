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
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepository.ValidationGroup;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests creation of {@link org.hibernate.validator.internal.metadata.raw.ConstrainedParameter} in
 * {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl}.
 *
 * @author Gunnar Morling
 */
public class ParameterMetaDataTest {

	private BeanMetaData<CustomerRepository> beanMetaData;

	@BeforeMethod
	public void setupBeanMetaData() {
		BeanMetaDataManager beanMetaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				new ExecutableHelper( new TypeResolutionHelper() )
		);

		beanMetaData = beanMetaDataManager.getBeanMetaData( CustomerRepository.class );
	}

	@Test
	public void constrainedParameterMetaData() throws Exception {
		Method method = CustomerRepository.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( 1 );

		assertFalse( parameterMetaData.isCascading() );
		assertTrue( parameterMetaData.isConstrained() );
		assertEquals( parameterMetaData.getIndex(), 1 );
		assertEquals( parameterMetaData.getName(), "arg1" );
		assertThat( parameterMetaData ).hasSize( 1 );
		assertEquals(
				parameterMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);
	}

	@Test
	public void cascadingParameterMetaData() throws Exception {
		Method method = CustomerRepository.class.getMethod( "saveCustomer", Customer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( 0 );

		assertTrue( parameterMetaData.isCascading() );
		assertTrue( parameterMetaData.isConstrained() );
		assertEquals( parameterMetaData.getIndex(), 0 );
		assertEquals( parameterMetaData.getName(), "arg0" );
		assertThat( parameterMetaData ).isEmpty();
	}

	@Test
	public void unconstrainedParameterMetaData() throws Exception {
		Method method = CustomerRepository.class.getMethod( "updateCustomer", Customer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( 0 );

		assertFalse( parameterMetaData.isCascading() );
		assertFalse( parameterMetaData.isConstrained() );
		assertThat( parameterMetaData ).isEmpty();
		assertFalse( parameterMetaData.requiresUnwrapping() );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void illegalParameterIndexCausesException() throws Exception {
		Method method = CustomerRepository.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		methodMetaData.getParameterMetaData( 0 );
	}

	@Test
	public void locallyDefinedGroupConversion() throws Exception {
		Method method = CustomerRepository.class.getMethod( "methodWithParameterGroupConversion", Set.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		assertThat(
				methodMetaData.getParameterMetaData( 0 )
						.convertGroup( Default.class )
		).isEqualTo( ValidationGroup.class );
	}

	@Test
	public void parameterRequiringUnwrapping() throws Exception {
		Method method = CustomerRepository.class.getMethod( "methodWithParameterRequiringUnwrapping", long.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( ExecutableElement.forMethod( method ) );

		ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( 0 );

		assertTrue( parameterMetaData.requiresUnwrapping() );
	}
}
