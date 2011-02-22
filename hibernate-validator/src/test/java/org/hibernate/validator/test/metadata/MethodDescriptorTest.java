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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.TypeDescriptor;
import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.util.Contracts.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Gunnar Morling
 */
public class MethodDescriptorTest {

	private TypeDescriptor typeDescriptor;

	@BeforeMethod
	public void setUpDescriptor() {

		typeDescriptor = TestUtil.getMethodValidator().getConstraintsForType( CustomerRepositoryExt.class );
		assertNotNull( typeDescriptor );
	}

	@Test
	public void testGetMethod() throws Exception {

		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		MethodDescriptor methodDescriptor = typeDescriptor.getConstraintsForMethod( method );

		assertNotNull( methodDescriptor );
		assertEquals( methodDescriptor.getMethod(), method );
	}

	/**
	 * The descriptor is retrieved using foo() from the base type, but it
	 * references foo() from the type represented by the type descriptor.
	 */
	@Test
	public void testGetMethodForOverriddenMethod() throws Exception {

		Method methodFromBaseType = CustomerRepository.class.getMethod( "foo" );
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		MethodDescriptor methodDescriptor = typeDescriptor.getConstraintsForMethod( methodFromBaseType );

		assertNotNull( methodDescriptor );
		assertEquals( methodDescriptor.getMethod(), method );
	}

	@Test
	public void testIsCascaded() throws Exception {

		Method cascadingMethod = CustomerRepositoryExt.class.getMethod( "foo" );
		MethodDescriptor cascadingMethodDescriptor = typeDescriptor.getConstraintsForMethod( cascadingMethod );

		assertNotNull( cascadingMethodDescriptor );
		assertTrue( cascadingMethodDescriptor.isCascaded() );

		Method nonCascadingMethod = CustomerRepositoryExt.class.getMethod( "baz" );
		MethodDescriptor nonCascadingMethodDescriptor = typeDescriptor.getConstraintsForMethod( nonCascadingMethod );

		assertNotNull( nonCascadingMethodDescriptor );
		assertFalse( nonCascadingMethodDescriptor.isCascaded() );
	}
}
