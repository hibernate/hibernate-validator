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

import org.slf4j.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

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
			Boy boy = new Boy();
			TestUtil.getValidator().validate( boy );
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Annotated methods must follow the JavaBeans naming convention. age() does not.",
					e.getMessage(),
					"Wrong error message"
			);
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullParamterToValidatorImplConstructor() {
		TestUtil.getValidator().getConstraintsForClass( null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
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
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );

		customer.setLastName( "Doe" );

		constraintViolations = validator.validate( customer );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
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
				constraintViolations.size(),
				3,
				"we should have been 2 not null violation for addresslines and one lenth violation for city"
		);

		constraintViolations = validator.validateProperty( address, "city" );
		assertEquals(
				constraintViolations.size(),
				1,
				"only city should be validated"
		);

		constraintViolations = validator.validateProperty( address, "city" );
		assertEquals(
				constraintViolations.size(),
				1,
				"only city should be validated"
		);

		constraintViolations = validator.validateValue( Address.class, "city", "Paris" );
		assertEquals(
				constraintViolations.size(),
				0,
				"Paris should be a valid city name."
		);
	}

	@Test
	public void testValidateList() {
		Validator validator = TestUtil.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );
		customer.setLastName( "Doe" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );

		Order order = new Order();
		customer.addOrder( order );

		constraintViolations = validator.validate( customer );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		assertEquals( "may not be null", constraintViolation.getMessage(), "Wrong message" );
		assertEquals( constraintViolation.getRootBean(), customer, "Wrong root entity" );
		assertEquals( constraintViolation.getInvalidValue(), order.getOrderNumber(), "Wrong value" );
		assertEquals( "orderList[0].orderNumber", constraintViolation.getPropertyPath(), "Wrong propertyName" );
	}

	@Test
	public void testMultiValueConstraint() {
		Validator validator = TestUtil.getValidator();

		Engine engine = new Engine();
		engine.setSerialNumber( "mail@foobar.com" );
		Set<ConstraintViolation<Engine>> constraintViolations = validator.validate( engine );
		assertEquals( constraintViolations.size(), 2, "Wrong number of constraints" );

		engine.setSerialNumber( "ABCDEFGH1234" );
		constraintViolations = validator.validate( engine );
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );

		engine.setSerialNumber( "ABCD-EFGH-1234" );
		constraintViolations = validator.validate( engine );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
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
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		assertEquals( "may not be empty", constraintViolation.getMessage(), "Wrong message" );
		assertEquals( constraintViolation.getRootBean(), clint, "Wrong root entity" );
		assertEquals( constraintViolation.getInvalidValue(), morgan.getLastName(), "Wrong value" );
		assertEquals(
				"playedWith[0].playedWith[1].lastName", constraintViolation.getPropertyPath(), "Wrong propertyName"
		);
	}

	@Test
	public void testValidateValue() {
		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validateValue(
				Customer.class, "orderList[0].orderNumber", null
		);
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		assertEquals( "may not be null", constraintViolation.getMessage(), "Wrong message" );
		assertEquals( constraintViolation.getRootBean(), null, "Wrong root entity" );
		assertEquals( constraintViolation.getRootBeanClass(), Customer.class, "Wrong root bean class" );
		assertEquals( constraintViolation.getInvalidValue(), null, "Wrong value" );
		assertEquals( "orderList[0].orderNumber", constraintViolation.getPropertyPath(), "Wrong propertyName" );

		constraintViolations = validator.validateValue( Customer.class, "orderList[0].orderNumber", 1234 );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
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
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		assertEquals( "may not be null", constraintViolation.getMessage(), "Wrong message" );
		assertEquals( constraintViolation.getRootBean(), customer, "Wrong root entity" );
		assertEquals( constraintViolation.getInvalidValue(), order.getOrderNumber(), "Wrong value" );
		assertEquals( "orderList[0].orderNumber", constraintViolation.getPropertyPath(), "Wrong propertyName" );

		order.setOrderNumber( 1234 );
		constraintViolations = validator.validateProperty( customer, "orderList[0].orderNumber" );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
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
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );

		assertConstraintViolation(
				constraintViolations.iterator().next(),
				"may not be null",
				Customer.class,
				null,
				"orderList[0].orderNumber"
		);

		order.setOrderNumber( 123 );

		constraintViolations = validator.validate( person );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
	}

	@Test
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
		assertEquals( constraintViolations.size(), 100, "Wrong number of constraints" );
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

		assertEquals( descriptors.size(), 1, "There should be only one constraint descriptor" );
		ConstraintDescriptor<?> descriptor = descriptors.iterator().next();
		Set<Class<?>> groups = descriptor.getGroups();
		assertTrue( groups.size() == 1, "There should be only one group" );
		assertEquals(
				groups.iterator().next(),
				Default.class,
				"The declared constraint does not explicitly define a group, hence Default is expected"
		);
	}
}
