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
package org.hibernate.validation.engine.metadata;

import java.util.Set;
import javax.validation.BeanDescriptor;
import javax.validation.PropertyDescriptor;
import javax.validation.Validator;

import org.slf4j.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

import org.hibernate.validation.engine.Order;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.TestUtil;


/**
 * @author Hardy Ferentschik
 */
public class BeanDescriptorTest {

	private static final Logger log = LoggerFactory.make();

	@Test
	public void testIsBeanConstrained() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Customer.class );

		// constraint via @Valid
		assertFalse( beanDescriptor.hasConstraints(), "There should be no direct constraints on the specified bean." );
		assertTrue( beanDescriptor.isBeanConstrained(), "Bean should be constrainted due to @valid " );

		// constraint hosted on bean itself
		beanDescriptor = validator.getConstraintsForClass( Account.class );
		assertTrue( beanDescriptor.hasConstraints(), "There should be direct constraints on the specified bean." );
		assertTrue( beanDescriptor.isBeanConstrained(), "Bean should be constrainted due to @valid" );

		// constraint on bean property
		beanDescriptor = validator.getConstraintsForClass( Order.class );
		assertFalse( beanDescriptor.hasConstraints(), "There should be no direct constraints on the specified bean." );
		assertTrue( beanDescriptor.isBeanConstrained(), "Bean should be constrainted due to @NotNull" );
	}

	@Test
	public void testUnconstraintClass() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( UnconstraintEntity.class );
		assertFalse( beanDescriptor.hasConstraints(), "There should be no direct constraints on the specified bean." );
		assertFalse( beanDescriptor.isBeanConstrained(), "Bean should be unconstrainted." );
	}

	@Test
	public void testGetConstraintForExistingConstrainedProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderNumber" );
		assertEquals(
				1, propertyDescriptor.getConstraintDescriptors().size(), "There should be one constraint descriptor"
		);

		beanDescriptor = validator.getConstraintsForClass( Customer.class );
		propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderList" );
		assertEquals(
				0, propertyDescriptor.getConstraintDescriptors().size(), "There should be no constraint descriptors"
		);
		assertTrue( propertyDescriptor.isCascaded(), "The property should be cascaded" );
	}

	@Test
	public void testGetConstraintForUnConstrainedProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Customer.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderList" );
		assertEquals(
				0, propertyDescriptor.getConstraintDescriptors().size(), "There should be no constraint descriptors"
		);
		assertTrue( propertyDescriptor.isCascaded(), "The property should be cascaded" );
	}

	@Test
	public void testGetConstraintsForNonExistingProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		assertNull( beanDescriptor.getConstraintsForProperty( "foobar" ), "There should be no descriptor" );
	}

	/**
	 * @todo Is this corect or should we get a IllegalArgumentException
	 */
	@Test
	public void testGetConstraintsForNullProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		assertNull( beanDescriptor.getConstraintsForProperty( null ), "There should be no descriptor" );
	}

	/**
	 * HV-95
	 */
	@Test
	public void testGetConstrainedProperties() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		Set<PropertyDescriptor> constraintProperties = beanDescriptor.getConstrainedProperties();
		assertEquals( 1, constraintProperties.size(), "There should be only one property" );
		boolean hasOrderNumber = false;
		for ( PropertyDescriptor pd : constraintProperties ) {
			hasOrderNumber |= pd.getPropertyName().equals( "orderNumber" );
		}
		assertTrue( hasOrderNumber, "Wrong property" );
	}

	/**
	 * HV-95
	 */
	@Test
	public void testGetConstrainedPropertiesImmutable() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		Set<PropertyDescriptor> constraintProperties = beanDescriptor.getConstrainedProperties();
		try {
			constraintProperties.add( null );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {
			log.trace( "success" );
		}

		try {
			constraintProperties.remove( constraintProperties.iterator().next() );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {
			log.trace( "success" );
		}
	}

	/**
	 * HV-95
	 */
	@Test
	public void testGetConstrainedPropertiesForUnconstraintEntity() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( UnconstraintEntity.class );
		Set<PropertyDescriptor> constraintProperties = beanDescriptor.getConstrainedProperties();
		assertEquals( 0, constraintProperties.size(), "We should get the empty set." );
	}
}
