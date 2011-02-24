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

import org.hibernate.validator.method.metadata.ParameterDescriptor;
import org.hibernate.validator.method.metadata.TypeDescriptor;
import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.util.Contracts.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Gunnar Morling
 */
public class ParameterDescriptorTest {

	private TypeDescriptor typeDescriptor;

	@BeforeMethod
	public void setUpDescriptor() {

		typeDescriptor = TestUtil.getMethodValidator().getConstraintsForType( CustomerRepositoryExt.class );
		assertNotNull( typeDescriptor );
	}

	@Test
	public void testGetElementClass() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", String.class, String.class );
		ParameterDescriptor parameterDescriptor = typeDescriptor.getConstraintsForMethod( method )
				.getParameterConstraints()
				.get( 0 );

		assertNotNull( parameterDescriptor );
		assertEquals( parameterDescriptor.getElementClass(), String.class );
	}

	@Test
	public void testHasConstraints() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", String.class, String.class );

		ParameterDescriptor parameterDescriptor1 = typeDescriptor.getConstraintsForMethod( method )
				.getParameterConstraints()
				.get( 0 );
		assertNotNull( parameterDescriptor1 );
		assertFalse( parameterDescriptor1.hasConstraints() );

		ParameterDescriptor parameterDescriptor2 = typeDescriptor.getConstraintsForMethod( method )
				.getParameterConstraints()
				.get( 1 );
		assertNotNull( parameterDescriptor2 );
		assertTrue( parameterDescriptor2.hasConstraints() );
	}

	@Test
	public void testGetConstraintDescriptors() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", String.class, String.class );

		ParameterDescriptor parameterDescriptor1 = typeDescriptor.getConstraintsForMethod( method )
				.getParameterConstraints()
				.get( 0 );
		assertNotNull( parameterDescriptor1 );
		assertTrue( parameterDescriptor1.getConstraintDescriptors().isEmpty() );

		ParameterDescriptor parameterDescriptor2 = typeDescriptor.getConstraintsForMethod( method )
				.getParameterConstraints()
				.get( 1 );
		assertNotNull( parameterDescriptor2 );
		assertEquals( parameterDescriptor2.getConstraintDescriptors().size(), 1 );

		assertEquals(
				parameterDescriptor2.getConstraintDescriptors().iterator().next().getAnnotation().annotationType(),
				NotNull.class
		);
	}

	@Test
	public void testGetIndex() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", String.class, String.class );

		ParameterDescriptor parameterDescriptor1 = typeDescriptor.getConstraintsForMethod( method )
				.getParameterConstraints()
				.get( 0 );
		assertNotNull( parameterDescriptor1 );
		assertEquals( parameterDescriptor1.getIndex(), 0 );

		ParameterDescriptor parameterDescriptor2 = typeDescriptor.getConstraintsForMethod( method )
				.getParameterConstraints()
				.get( 1 );
		assertNotNull( parameterDescriptor2 );
		assertEquals( parameterDescriptor2.getIndex(), 1 );
	}

	@Test
	public void testIsCascaded() throws Exception {

		Method method1 = CustomerRepositoryExt.class.getMethod( "createCustomer", String.class, String.class );

		ParameterDescriptor parameterDescriptor1 = typeDescriptor.getConstraintsForMethod( method1 )
				.getParameterConstraints()
				.get( 0 );
		assertNotNull( parameterDescriptor1 );
		assertFalse( parameterDescriptor1.isCascaded() );

		Method method2 = CustomerRepositoryExt.class.getMethod( "saveCustomer", Customer.class );

		ParameterDescriptor parameterDescriptor2 = typeDescriptor.getConstraintsForMethod( method2 )
				.getParameterConstraints()
				.get( 0 );
		assertNotNull( parameterDescriptor2 );
		assertTrue( parameterDescriptor2.isCascaded() );
	}

}
