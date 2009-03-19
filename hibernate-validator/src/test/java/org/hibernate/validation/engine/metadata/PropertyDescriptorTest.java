// $Id: ElementDescriptorTest.java 16185 2009-03-19 13:55:43Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.metadata;

import javax.validation.PropertyDescriptor;
import javax.validation.Validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.hibernate.validation.engine.Order;
import org.hibernate.validation.util.TestUtil;


/**
 * @author Hardy Ferentschik
 */
public class PropertyDescriptorTest {
	@Test
	public void testIsNotCascaded() {
		Validator validator = TestUtil.getValidator();
		PropertyDescriptor descriptor = validator.getConstraintsForClass( Order.class )
				.getConstraintsForProperty( "orderNumber" );
		assertFalse( "Should not be cascaded", descriptor.isCascaded() );
	}

	@Test
	public void testIsCascaded() {
		Validator validator = TestUtil.getValidator();
		PropertyDescriptor descriptor = validator.getConstraintsForClass( Customer.class )
				.getConstraintsForProperty( "orderList" );
		assertTrue( "Should be cascaded", descriptor.isCascaded() );
	}

	@Test
	public void testPropertyName() {
		Validator validator = TestUtil.getValidator();
		String propertyName = "orderList";
		PropertyDescriptor descriptor = validator.getConstraintsForClass( Customer.class )
				.getConstraintsForProperty( propertyName );
		assertEquals( "Wrong property name", propertyName, descriptor.getPropertyName() );
	}
}