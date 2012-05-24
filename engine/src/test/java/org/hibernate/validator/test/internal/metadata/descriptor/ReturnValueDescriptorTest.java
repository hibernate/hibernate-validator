/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.metadata.descriptor;

import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.CustomerRepository;

import static org.hibernate.validator.testutil.ValidatorUtil.getMethodDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
@Test
public class ReturnValueDescriptorTest {

	@Test
	public void testElementDescriptorType() {
		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepository.class, "foo" );
		ReturnValueDescriptor returnValueDescriptor = methodDescriptor.getReturnValueDescriptor();

		assertEquals( returnValueDescriptor.getKind(), ElementDescriptor.Kind.RETURN_VALUE );
	}

	@Test
	public void testIsCascaded() {
		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepository.class, "foo" );
		ReturnValueDescriptor returnValueDescriptor = methodDescriptor.getReturnValueDescriptor();

		assertTrue( returnValueDescriptor.isCascaded() );
	}

	@Test
	public void testIsNotCascaded() {
		MethodDescriptor methodDescriptor = getMethodDescriptor( CustomerRepository.class, "bar" );
		ReturnValueDescriptor returnValueDescriptor = methodDescriptor.getReturnValueDescriptor();

		assertFalse( returnValueDescriptor.isCascaded() );
	}
}
