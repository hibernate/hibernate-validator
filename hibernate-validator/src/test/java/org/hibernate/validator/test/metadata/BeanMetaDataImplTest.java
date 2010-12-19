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
import javax.validation.ValidationException;
import javax.validation.constraints.Min;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.metadata.BeanMetaData;
import org.hibernate.validator.metadata.BeanMetaDataCache;
import org.hibernate.validator.metadata.BeanMetaDataImpl;
import org.hibernate.validator.metadata.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MethodMetaData;
import org.hibernate.validator.metadata.ReturnValueMetaData;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepository;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link BeanMetaData}.
 *
 * @author Gunnar Morling
 */
public class BeanMetaDataImplTest {

	private BeanMetaData<CustomerRepository> metaData;

	@BeforeMethod
	public void setUpMetaData() {
		metaData = new BeanMetaDataImpl<CustomerRepository>(
				CustomerRepository.class, new ConstraintHelper(), true, new BeanMetaDataCache()
		);
	}

	@Test
	public void nonCascadingConstraintAtMethodReturnValue() throws Exception {

		Method method = CustomerRepository.class.getMethod( "baz" );
		MethodMetaData methodMetaData = metaData.getMetaDataForMethod( method ).get( CustomerRepository.class );
		ReturnValueMetaData returnValueMetaData = methodMetaData.getReturnValueMetaData();
		ConstraintDescriptorImpl<? extends Annotation> descriptor = returnValueMetaData.iterator()
				.next()
				.getDescriptor();

		assertEquals( methodMetaData.getMethod(), method );
		assertEquals( descriptor.getAnnotation().annotationType(), Min.class );
		assertEquals( returnValueMetaData.iterator().next().getDescriptor().getAttributes().get( "value" ), 10L );
		assertFalse( returnValueMetaData.isCascading() );
	}

	@Test
	public void cascadingConstraintAtMethodReturnValue() throws Exception {

		Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
		MethodMetaData methodMetaData = metaData.getMetaDataForMethod( method ).get( CustomerRepository.class );
		ReturnValueMetaData returnValueMetaData = methodMetaData.getReturnValueMetaData();

		assertEquals( methodMetaData.getMethod(), method );
		assertFalse( returnValueMetaData.iterator().hasNext() );
		assertTrue( returnValueMetaData.isCascading() );
	}

	/**
	 * The JSR 303 TCK mandates that a compliant implementation must throw an exception in case a non-getter method
	 * is annotated with a constraint annotation. To be compliant and support method validation we have a switch ({@link HibernateValidatorConfiguration#allowMethodLevelConstraints()} which controls this behavior.
	 */
	@Test(expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "Annotated methods must follow the JavaBeans naming convention.*")
	public void constraintAtMethodReturnValueCausesExceptionDueToMethodValidationNotBeingEnabled() {

		metaData = new BeanMetaDataImpl<CustomerRepository>(
				CustomerRepository.class, new ConstraintHelper(), false, new BeanMetaDataCache()
		);
	}
}
