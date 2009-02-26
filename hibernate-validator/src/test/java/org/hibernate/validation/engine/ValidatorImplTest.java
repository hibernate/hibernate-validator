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
import javax.validation.ConstraintViolation;
import javax.validation.PropertyDescriptor;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.slf4j.Logger;

import org.hibernate.validation.eg.Actor;
import org.hibernate.validation.eg.Address;
import org.hibernate.validation.eg.Boy;
import org.hibernate.validation.eg.Customer;
import org.hibernate.validation.eg.Engine;
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.eg.Person;
import org.hibernate.validation.eg.UnconstraintEntity;
import org.hibernate.validation.eg.groups.First;
import org.hibernate.validation.eg.groups.Last;
import org.hibernate.validation.eg.groups.Second;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.TestUtil;
import static org.hibernate.validation.util.TestUtil.assertConstraintViolation;

/**
 * Tests for the implementation of <code>Validator</code>.
 *
 * @author Hardy Ferentschik
 */
public class ValidatorImplTest {

	private static final Logger log = LoggerFactory.make();

	@Test
	public void testWrongMethodName() {
		try {
			TestUtil.getValidator().getConstraintsForClass( Boy.class ).hasConstraints();
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong error message",
					"Annotated methods must follow the JavaBeans naming convention. age() does not.",
					e.getMessage()
			);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullParamterToValidatorImplConstructor() {
		TestUtil.getValidator().getConstraintsForClass( null );
	}

	@Test
	public void testUnconstraintClass() {
		Validator validator = TestUtil.getValidator();
		assertTrue(
				"There should be no constraints",
				!validator.getConstraintsForClass( UnconstraintEntity.class ).hasConstraints()
		);
	}

	@Test
	public void testHasConstraintsAndIsBeanConstrained() {
		Validator validator = TestUtil.getValidator();
		assertTrue(
				"There should not be constraints", !validator.getConstraintsForClass( Customer.class ).hasConstraints()
		);
		assertTrue(
				"It should be constrainted", validator.getConstraintsForClass( Customer.class ).isBeanConstrained()
		);
		// TODO fix test
//		assertTrue( "It should be constrainted even if it has no constraint annotations - not implemented yet", validator.getConstraintsForClass( Account.class ).isBeanConstrained() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateWithNull() {
		Validator validator = TestUtil.getValidator();
		validator.validate( null );
	}

	@Test
	@SuppressWarnings("NullArgumentToVariableArgMethod")
	public void testPassingNullAsGroup() {
		Validator validator = TestUtil.getValidator();
		Customer customer = new Customer();
		try {
			validator.validate( customer, null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			log.trace( "success" );
		}

		try {
			validator.validateProperty( customer, "firstName", null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			log.trace( "success" );
		}

		try {
			validator.validateValue( Customer.class, "firstName", "foobar", null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			log.trace( "success" );
		}
	}

	@Test
	public void testValidateWithNullProperty() {
		Validator validator = TestUtil.getValidator();
		try {
			validator.validate( null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			log.trace( "success" );
		}

		try {
			validator.validateProperty( null, "firstName" );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			log.trace( "success" );
		}

		try {
			validator.validateValue( null, "firstName", "foobar" );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			log.trace( "success" );
		}
	}

	@Test
	public void testBasicValidation() {
		Validator validator = TestUtil.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		customer.setLastName( "Doe" );

		constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testMultipleValidationMethods() {
		Validator validator = TestUtil.getValidator();

		Address address = new Address();
		address.setAddressline1( null );
		address.setAddressline2( null );
		address.setCity( "Llanfairpwllgwyngyllgogerychwyrndrobwyll-llantysiliogogogoch" ); //town in North Wales

		Set<ConstraintViolation<Address>> constraintViolations = validator.validate( address );
		assertEquals(
				"we should have been 2 not null violation for addresslines and one lenth violation for city",
				3,
				constraintViolations.size()
		);

		constraintViolations = validator.validateProperty( address, "city" );
		assertEquals(
				"only city should be validated",
				1,
				constraintViolations.size()
		);

		constraintViolations = validator.validateProperty( address, "city" );
		assertEquals(
				"only city should be validated",
				1,
				constraintViolations.size()
		);

		constraintViolations = validator.validateValue( Address.class, "city", "Paris" );
		assertEquals(
				"Paris should be a valid city name.",
				0,
				constraintViolations.size()
		);
	}

	@Test
	public void testValidateList() {
		Validator validator = TestUtil.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );
		customer.setLastName( "Doe" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );

		Order order = new Order();
		customer.addOrder( order );

		constraintViolations = validator.validate( customer );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", customer, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", order.getOrderNumber(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraintViolation.getPropertyPath() );

	}

	@Test
	public void testMultiValueConstraint() {
		Validator validator = TestUtil.getValidator();

		Engine engine = new Engine();
		engine.setSerialNumber( "mail@foobar.com" );
		Set<ConstraintViolation<Engine>> constraintViolations = validator.validate( engine );
		assertEquals( "Wrong number of constraints", 2, constraintViolations.size() );

		engine.setSerialNumber( "ABCDEFGH1234" );
		constraintViolations = validator.validate( engine );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		engine.setSerialNumber( "ABCD-EFGH-1234" );
		constraintViolations = validator.validate( engine );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testGraphValidation() {
		Validator validator = TestUtil.getValidator();

		Actor clint = new Actor( "Clint", "Eastwood" );
		Actor morgan = new Actor( "Morgan", "" );
		Actor charlie = new Actor( "Charlie", "Sheen" );

		clint.addPlayedWith( charlie );
		charlie.addPlayedWith( clint );
		charlie.addPlayedWith( morgan );
		morgan.addPlayedWith( charlie );
		morgan.addPlayedWith( clint );
		clint.addPlayedWith( morgan );


		Set<ConstraintViolation<Actor>> constraintViolations = validator.validate( clint );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be empty", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", clint, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", morgan.getLastName(), constraintViolation.getInvalidValue() );
		assertEquals(
				"Wrong propertyName", "playedWith[0].playedWith[1].lastName", constraintViolation.getPropertyPath()
		);
	}

	@Test
	public void testValidateValue() {
		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validateValue(
				Customer.class, "orderList[0].orderNumber", null
		);
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", null, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", null, constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraintViolation.getPropertyPath() );

		constraintViolations = validator.validateValue( Customer.class, "orderList[0].orderNumber", "1234" );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testValidateValueWithInvalidPropertyPath() {
		Validator validator = TestUtil.getValidator();

		try {
			validator.validateValue( Customer.class, "", null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			assertEquals( "Invalid property path.", e.getMessage() );
		}

		try {
			validator.validateValue( Customer.class, "foobar", null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			assertEquals( "Invalid property path.", e.getMessage() );
		}

		try {
			validator.validateValue( Customer.class, "orderList[0].foobar", null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			assertEquals( "Invalid property path.", e.getMessage() );
		}
	}

	@Test
	public void testValidateProperty() {
		Validator validator = TestUtil.getValidator();

		Customer customer = new Customer();
		Order order = new Order();
		customer.addOrder( order );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validateProperty(
				customer, "orderList[0].orderNumber"
		);
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", customer, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", order.getOrderNumber(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraintViolation.getPropertyPath() );

		order.setOrderNumber( 1234 );
		constraintViolations = validator.validateProperty( customer, "orderList[0].orderNumber" );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testValidatePropertyWithInvalidPropertyPath() {
		Validator validator = TestUtil.getValidator();

		Customer customer = new Customer();
		Order order = new Order();
		customer.addOrder( order );

		try {
			validator.validateProperty( customer, "orderList[1].orderNumber" );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			assertEquals( "Invalid property path.", e.getMessage() );
		}

		try {
			validator.validateProperty( customer, "" );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			assertEquals( "Invalid property path.", e.getMessage() );
		}

		try {
			validator.validateProperty( customer, "foobar" );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			assertEquals( "Invalid property path.", e.getMessage() );
		}

		try {
			validator.validateProperty( customer, "orderList[0].foobar" );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			assertEquals( "Invalid property path.", e.getMessage() );
		}
	}

	/**
	 * HV-108
	 */
	@Test
	public void testValidationIsPolymorphic() {
		Validator validator = TestUtil.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "Foo" );
		customer.setLastName( "Bar" );

		Order order = new Order();
		customer.addOrder( order );

		Person person = customer;

		Set<ConstraintViolation<Person>> constraintViolations = validator.validate( person );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		assertConstraintViolation(
				constraintViolations.iterator().next(),
				"may not be null",
				Customer.class,
				null,
				"orderList[0].orderNumber"
		);

		order.setOrderNumber( 123 );

		constraintViolations = validator.validate( person );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	/**
	 * HV-120
	 */
	@Test
	public void testConstraintDescriptorWithoutExplicitGroup() {
		Validator validator = TestUtil.getValidator();

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderNumber" );
		Set<ConstraintDescriptor<?>> descriptors = propertyDescriptor.getConstraintDescriptors();

		assertEquals( "There should be only one constraint descriptor", 1, descriptors.size() );
		ConstraintDescriptor descriptor = descriptors.iterator().next();
		Set<Class<?>> groups = descriptor.getGroups();
		assertTrue( "There should be only one group", groups.size() == 1 );
		assertEquals(
				"The declared constraint does not explicitly define a group, hence Default is expected",
				Default.class,
				groups.iterator().next()
		);
	}

	@org.junit.Test
	public void testObjectTraversion() {
		Validator validator = TestUtil.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );
		customer.setLastName( "Doe" );

		for ( int i = 0; i < 100; i++ ) {
			Order order = new Order();
			customer.addOrder( order );
		}

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate(
				customer, Default.class, First.class, Second.class, Last.class
		);
		assertEquals( "Wrong number of constraints", 100, constraintViolations.size() );
	}
}
