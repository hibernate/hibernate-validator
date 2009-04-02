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
package org.hibernate.validation.engine.graphnavigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import org.hibernate.validation.util.TestUtil;

/**
 * @author Hardy Ferentschik
 */
public class GraphNavigationTest {

	@Test
	public void testGraphNavigationDeterminism() {
		// build the test object graph
		User user = new User( "John", "Doe" );

		Address address1 = new Address( null, "11122", "Stockholm" );
		address1.setInhabitant( user );

		Address address2 = new Address( "Kungsgatan 5", "11122", "Stockholm" );
		address2.setInhabitant( user );

		user.addAddress( address1 );
		user.addAddress( address2 );

		Order order = new Order( 1 );
		order.setShippingAddress( address1 );
		order.setBillingAddress( address2 );
		order.setCustomer( user );

		OrderLine line1 = new OrderLine( order, 42 );
		OrderLine line2 = new OrderLine( order, 101 );
		order.addOrderLine( line1 );
		order.addOrderLine( line2 );

		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<Order>> constraintViolations = validator.validate( order );
		assertEquals( 3, constraintViolations.size(), "Wrong number of constraints" );

		List<String> expectedErrorMessages = new ArrayList<String>();
		expectedErrorMessages.add( "shippingAddress.addressline1" );
		expectedErrorMessages.add( "customer.addresses[0].addressline1" );
		expectedErrorMessages.add( "billingAddress.inhabitant.addresses[0].addressline1" );

		for ( ConstraintViolation<Order> violation : constraintViolations ) {
			if ( expectedErrorMessages.contains( violation.getPropertyPath() ) ) {
				expectedErrorMessages.remove( violation.getPropertyPath() );
			}
		}

		assertTrue( expectedErrorMessages.size() == 0, "All error messages should have occured once" );
	}

	@Test
	public void testNoEndlessLoop() {
		User john = new User( "John", null );
		john.knows( john );

		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( john );
		assertEquals( 1, constraintViolations.size(), "Wrong number of constraints" );
		TestUtil.assertConstraintViolation(
				constraintViolations.iterator().next(), "may not be null", User.class, null, "lastName"
		);


		User jane = new User( "Jane", "Doe" );
		jane.knows( john );
		john.knows( jane );

		constraintViolations = validator.validate( john );
		assertEquals( 1, constraintViolations.size(), "Wrong number of constraints" );
		TestUtil.assertConstraintViolation(
				constraintViolations.iterator().next(), "may not be null", User.class, null, "lastName"
		);

		constraintViolations = validator.validate( jane );
		assertEquals( 1, constraintViolations.size(), "Wrong number of constraints" );
		TestUtil.assertConstraintViolation(
				constraintViolations.iterator().next(), "may not be null", User.class, null, "knowsUser[0].lastName"
		);

		john.setLastName( "Doe" );
		constraintViolations = validator.validate( john );
		assertEquals( 0, constraintViolations.size(), "Wrong number of constraints" );
	}
}
