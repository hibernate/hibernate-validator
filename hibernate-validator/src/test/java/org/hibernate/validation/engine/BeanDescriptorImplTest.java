// $Id$
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
package org.hibernate.validation.engine;

import java.util.Set;
import javax.validation.BeanDescriptor;
import javax.validation.ConstraintDescriptor;
import javax.validation.ElementDescriptor;
import javax.validation.PropertyDescriptor;
import javax.validation.Validator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.hibernate.validation.eg.Account;
import org.hibernate.validation.eg.Customer;
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.eg.UnconstraintEntity;
import org.hibernate.validation.util.TestUtil;


/**
 * @author Hardy Ferentschik
 */
public class BeanDescriptorImplTest {

	@Test
	public void testHasConstraintsAndIsBeanConstrained() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Customer.class );

		assertFalse( "There should be no direct constraints on the specified bean.", beanDescriptor.hasConstraints() );
		assertTrue( "Bean should be constrainted due to @valid ", beanDescriptor.isBeanConstrained() );

		beanDescriptor = validator.getConstraintsForClass( Account.class );
		assertTrue(
				"Bean should be constrainted due to @valid", beanDescriptor.isBeanConstrained()
		);
	}

	@Test
	public void testUnconstraintClass() {
		Validator validator = TestUtil.getValidator();
		assertFalse(
				"There should be no constraints",
				validator.getConstraintsForClass( UnconstraintEntity.class ).hasConstraints()
		);
	}

	@Test
	public void testGetConstraintsForProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderNumber" );
		assertEquals(
				"There should be one constraint descriptor", 1, propertyDescriptor.getConstraintDescriptors().size()
		);

		assertNull( "There should be no descriptor", beanDescriptor.getConstraintsForProperty( "foobar" ) );

		// TODO Is this corect or should we get a IllegalArgumentException
		assertNull( "There should be no descriptor", beanDescriptor.getConstraintsForProperty( null ) );

		beanDescriptor = validator.getConstraintsForClass( Customer.class );
		propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderList" );
		assertEquals(
				"There should be no constraint descriptors", 0, propertyDescriptor.getConstraintDescriptors().size()
		);
		assertTrue( "The property should be cascaded", propertyDescriptor.isCascaded() );
	}

	/**
	 * HV-95
	 */
	@Test
	public void testGetConstrainedProperties() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		Set<String> constraintProperties = beanDescriptor.getConstrainedProperties();
		assertEquals( "There should be only one property", 1, constraintProperties.size() );
		assertTrue( "Wrong property", constraintProperties.contains( "orderNumber" ) );

		try {
			constraintProperties.add( "foobar" );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}

		try {
			constraintProperties.remove( "orderNumber" );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}
	}

	/**
	 * HV-95
	 */
	@Test
	public void testElementDescriptorImmutable() {
		Validator validator = TestUtil.getValidator();
		ElementDescriptor elementDescriptor = validator.getConstraintsForClass( Order.class )
				.getConstraintsForProperty( "orderNumber" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = elementDescriptor.getConstraintDescriptors();
		assertTrue( "There should be a ConstraintDescriptor", constraintDescriptors.size() == 1 );
		ConstraintDescriptor<?> descriptor = constraintDescriptors.iterator().next();

		try {
			constraintDescriptors.add( descriptor );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}

		try {
			constraintDescriptors.remove( descriptor );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}
	}
}
