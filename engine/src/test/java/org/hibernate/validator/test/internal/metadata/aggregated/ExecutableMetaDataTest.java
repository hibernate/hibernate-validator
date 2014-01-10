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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.validation.ElementKind;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.joda.time.DateMidnight;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
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
		BeanMetaDataManager beanMetaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				new ExecutableHelper( new TypeResolutionHelper() )
		);

		beanMetaData = beanMetaDataManager.getBeanMetaData( CustomerRepositoryExt.class );
	}

	@Test
	public void getNameOfMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getName() ).isEqualTo( method.getName() );
	}

	@Test
	public void getNameOfConstructor() throws Exception {
		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forConstructor(
						constructor
				)
		);

		assertThat( constructorMetaData.getName() ).isEqualTo( constructor.getDeclaringClass().getSimpleName() );
	}

	@Test
	public void getTypeForMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getType() ).isEqualTo( Customer.class );
	}

	@Test
	public void getTypeForVoidMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "zap" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getType() ).isEqualTo( void.class );
	}

	@Test
	public void getTypeForConstructor() throws Exception {
		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forConstructor(
						constructor
				)
		);

		assertThat( constructorMetaData.getType() ).isEqualTo( CustomerRepositoryExt.class );
	}

	@Test
	public void getParameterTypes() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getParameterTypes() ).isEqualTo( method.getParameterTypes() );
	}

	@Test
	public void getParameterTypesForParameterlessExcutable() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getParameterTypes() ).isEqualTo( method.getParameterTypes() );
	}

	@Test
	public void getKindForMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getKind() ).isEqualTo( ElementKind.METHOD );
	}

	@Test
	public void getKindForConstructor() throws Exception {

		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forConstructor(
						constructor
				)
		);

		assertThat( constructorMetaData.getKind() ).isEqualTo( ElementKind.CONSTRUCTOR );
	}

	@Test
	public void getIdentifierForMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getIdentifier() ).isEqualTo(
				"createCustomer[interface java.lang.CharSequence, class java.lang.String]"
		);
	}

	@Test
	public void getIdentifierForParameterlessMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData.getIdentifier() ).isEqualTo( "foo[]" );
	}

	@Test
	public void getIdentifierForConstructor() throws Exception {
		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forConstructor(
						constructor
				)
		);

		assertThat( constructorMetaData.getIdentifier() ).isEqualTo( "CustomerRepositoryExt[class java.lang.String]" );
	}

	@Test
	public void requiresUnwrappingForMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "methodRequiringUnwrapping" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertTrue( methodMetaData.requiresUnwrapping() );
	}

	@Test
	public void requiresUnwrappingForConstructor() throws Exception {
		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( long.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forConstructor(
						constructor
				)
		);

		assertTrue( constructorMetaData.requiresUnwrapping() );
	}

	@Test
	public void methodWithConstrainedParameter() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		assertFalse( methodMetaData.getParameterMetaData( 0 ).isConstrained() );
		assertFalse( methodMetaData.getParameterMetaData( 0 ).isCascading() );

		assertTrue( methodMetaData.getParameterMetaData( 1 ).isConstrained() );
		assertFalse( methodMetaData.getParameterMetaData( 1 ).isCascading() );
		assertThat( methodMetaData.getParameterMetaData( 1 ) ).hasSize( 1 );
		assertEquals(
				methodMetaData.getParameterMetaData( 1 )
						.iterator()
						.next()
						.getDescriptor()
						.getAnnotation()
						.annotationType(),
				NotNull.class
		);

		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void methodWithCascadedParameter() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "saveCustomer", Customer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		assertTrue( methodMetaData.getParameterMetaData( 0 ).isConstrained() );
		assertTrue( methodMetaData.getParameterMetaData( 0 ).isCascading() );
		assertThat( methodMetaData.getParameterMetaData( 0 ) ).isEmpty();

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
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

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
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).hasSize( 1 );
		assertEquals(
				methodMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);

		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void returnValueConstraintFromSuperType() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "bar" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertThat( methodMetaData ).hasSize( 1 );
		assertFalse( methodMetaData.isCascading() );

		ConstraintDescriptorImpl<? extends Annotation> descriptor = methodMetaData.iterator()
				.next()
				.getDescriptor();
		assertEquals( descriptor.getAnnotation().annotationType(), NotNull.class );
	}

	@Test
	public void returnValueConstraintsAddUpInHierarchy() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "baz" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).hasSize( 2 );
	}

	@Test
	public void methodWithCascadedReturnValue() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertTrue( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void locallyDefinedGroupConversion() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "methodWithReturnValueGroupConversion" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

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
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor(
				ExecutableElement.forMethod(
						method
				)
		);

		assertFalse( methodMetaData.isCascading() );
		assertFalse( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
		assertFalse( methodMetaData.requiresUnwrapping() );

		assertThat( methodMetaData.getParameterMetaData( 0 ).isConstrained() ).isFalse();
		assertThat( methodMetaData.getParameterMetaData( 0 ).isCascading() ).isFalse();
		assertThat( methodMetaData.getParameterMetaData( 0 ) ).isEmpty();
	}
}
