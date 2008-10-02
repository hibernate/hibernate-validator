// $Id: ValidatorImplTest.java 105 2008-09-29 12:37:32Z hardy.ferentschik $
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

import java.util.HashSet;
import java.util.Set;
import javax.validation.InvalidConstraint;
import javax.validation.ValidationException;
import javax.validation.Validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hibernate.validation.eg.Actor;
import org.hibernate.validation.eg.Address;
import org.hibernate.validation.eg.Animal;
import org.hibernate.validation.eg.Author;
import org.hibernate.validation.eg.Book;
import org.hibernate.validation.eg.Boy;
import org.hibernate.validation.eg.Customer;
import org.hibernate.validation.eg.Dictonary;
import org.hibernate.validation.eg.Engine;
import org.hibernate.validation.eg.EnglishDictonary;
import org.hibernate.validation.eg.Female;
import org.hibernate.validation.eg.Male;
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.eg.Unconstraint;

/**
 * Tests for the implementation of <code>Validator</code>.
 *
 * @author Hardy Ferentschik
 */
public class ValidatorImplTest {

	/**
	 * JSR 303: Constraint definition properties - message (2.1.1.1)
	 */
	@Test
	public void testConstraintWithNoMessage() {
		try {
			new ValidatorImpl<Male>( Male.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong error message", "Constraint annotation has to define message element.", e.getMessage()
			);
		}
	}

	/**
	 * JSR 303: Constraint definition properties - groups (2.1.1.2)
	 */
	@Test
	public void testConstraintWithNoGroups() {
		try {
			new ValidatorImpl<Female>( Female.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong error message", "Constraint annotation has to define groups element.", e.getMessage()
			);
		}
	}

	/**
	 * JSR 303: Requirements on classes to be validates (3.1)
	 */
	@Test
	public void testWrongMethodName() {
		try {
			new ValidatorImpl<Boy>( Boy.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong error message",
					"Annoated methods must follow the JavaBeans naming convention. age() does not.",
					e.getMessage()
			);
		}
	}


	@Test( expected = IllegalArgumentException.class)
	public void testNullParamterToValidatorImplConstructor() {
		new ValidatorImpl<Unconstraint>( null);
	}

	@Test	
	public void testUnconstraintClass() {
		Validator<Unconstraint> validator = new ValidatorImpl<Unconstraint>( Unconstraint.class );
		assertTrue( "There should be no constraints", !validator.hasConstraints() );
	}

	@Test
	public void testHasConstraints() {
		Validator<Customer> validatorCustomer = new ValidatorImpl<Customer>( Customer.class );
		assertTrue( "There should be constraints", validatorCustomer.hasConstraints() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateWithNull() {
		Validator<Customer> validatorCustomer = new ValidatorImpl<Customer>( Customer.class );
		validatorCustomer.validate( null );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateWithNullProperty() {
		Validator<Customer> validatorCustomer = new ValidatorImpl<Customer>( Customer.class );
		validatorCustomer.validate( null, "firstName" );
	}

	@Test
	public void testGroups() {
		Validator<Book> validator = new ValidatorImpl<Book>( Book.class );

		Author author = new Author();
		author.setLastName( "" );
		author.setFirstName( "" );
		Book book = new Book();
		book.setTitle( "" );
		book.setAuthor( author );

		Set<InvalidConstraint<Book>> invalidConstraints = validator.validate( book, "first", "second", "last" );
		assertEquals( "Wrong number of constraints", 3, invalidConstraints.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		invalidConstraints = validator.validate( book, "first", "second", "last" );
		InvalidConstraint constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		assertEquals( "Wrong message", "may not be empty", constraint.getMessage() );
		assertEquals( "Wrong bean class", Book.class, constraint.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraint.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraint.getValue() );
		assertEquals( "Wrong propertyName", "title", constraint.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		invalidConstraints = validator.validate( book, "first", "second", "last" );
		constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		assertEquals( "Wrong message", "length must be between 0 and 30", constraint.getMessage() );
		assertEquals( "Wrong bean class", Book.class, constraint.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraint.getRootBean() );
		assertEquals( "Wrong value", book.getSubtitle(), constraint.getValue() );
		assertEquals( "Wrong propertyName", "subtitle", constraint.getPropertyPath() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		invalidConstraints = validator.validate( book, "first", "second", "last" );
		constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		assertEquals( "Wrong message", "length must be between 0 and 20", constraint.getMessage() );
		assertEquals( "Wrong bean class", Author.class, constraint.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraint.getRootBean() );
		assertEquals( "Wrong value", author.getCompany(), constraint.getValue() );
		assertEquals( "Wrong propertyName", "author.company", constraint.getPropertyPath() );

		author.setCompany( "JBoss" );

		invalidConstraints = validator.validate( book, "first", "second", "last" );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
	}

	@Test
	public void testDefaultGroupSequence() {
		Validator<Book> validator = new ValidatorImpl<Book>( Book.class );

		Author author = new Author();
		author.setLastName( "" );
		author.setFirstName( "" );
		Book book = new Book();
		book.setAuthor( author );

		Set<InvalidConstraint<Book>> invalidConstraints = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 2, invalidConstraints.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		invalidConstraints = validator.validate( book, "default" );
		InvalidConstraint constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		assertEquals( "Wrong message", "may not be null", constraint.getMessage() );
		assertEquals( "Wrong bean class", Book.class, constraint.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraint.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraint.getValue() );
		assertEquals( "Wrong propertyName", "title", constraint.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		invalidConstraints = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		invalidConstraints = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );

		author.setCompany( "JBoss" );

		invalidConstraints = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
	}

	@Test
	public void testBasicValidation() {
		Validator<Customer> validator = new ValidatorImpl<Customer>( Customer.class );

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<InvalidConstraint<Customer>> invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );

		customer.setLastName( "Doe" );

		invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
	}

	@Test
	public void testGroupSequences() {
		Validator<Dictonary> validator = new ValidatorImpl<Dictonary>( Dictonary.class );

		Dictonary dictonary = new Dictonary();
		dictonary.setTitle( "English - German" );
		Author author = new Author();
		author.setLastName( "-" );
		author.setFirstName( "-" );
		author.setCompany( "Langenscheidt Publ." );
		dictonary.setAuthor( author );

		Set<InvalidConstraint<Dictonary>> invalidConstraints = validator.validate( dictonary, "default-alias" );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
	}

	@Test
	public void testValidationFailureInMultipleGroups() {
		Validator<Animal> validator = new ValidatorImpl<Animal>( Animal.class );
		Animal elepfant = new Animal();
		elepfant.setName( "" );
		elepfant.setDomain( Animal.Domain.EUKARYOTA );

		Set<InvalidConstraint<Animal>> invalidConstraints = validator.validate( elepfant, "first", "second" );
		assertEquals(
				"The should be two invalid constraints since the same propertyName gets validated in both groups",
				1,
				invalidConstraints.size()
		);

		InvalidConstraint constraint = invalidConstraints.iterator().next();
		Set<String> expected = new HashSet<String>();
		expected.add( "first" );
		expected.add( "second" );
		assertEquals(
				"The constraint should be invalid for both groups",
				expected,
				constraint.getGroups()
		);
	}

	@Test(expected = ValidationException.class)
	public void testInvalidSequenceName() {
		new ValidatorImpl<EnglishDictonary>( EnglishDictonary.class );
	}

	@Test
	public void testValidationMethod() {
		Validator<Address> validator = new ValidatorImpl<Address>( Address.class );

		Address address = new Address();
		address.setAddressline1( null );
		address.setAddressline2( null );
		address.setCity( "Llanfairpwllgwyngyllgogerychwyrndrobwyll-llantysiliogogogoch" ); //town in North Wales

		Set<InvalidConstraint<Address>> invalidConstraints = validator.validate( address );
		assertEquals(
				"we should have been 2 not null violation for addresslines and one lenth violation for city",
				3,
				invalidConstraints.size()
		);

		invalidConstraints = validator.validateProperty( address, "city" );
		assertEquals(
				"only city should be validated",
				1,
				invalidConstraints.size()
		);

		invalidConstraints = validator.validateProperty( address, "city" );
		assertEquals(
				"only city should be validated",
				1,
				invalidConstraints.size()
		);

		invalidConstraints = validator.validateValue( "city", "Paris" );
		assertEquals(
				"Paris should be a valid city name.",
				0,
				invalidConstraints.size()
		);
	}

	@Test
	public void testValidateList() {
		Validator<Customer> validator = new ValidatorImpl<Customer>( Customer.class );

		Customer customer = new Customer();
		customer.setFirstName( "John" );
		customer.setLastName( "Doe" );

		Set<InvalidConstraint<Customer>> invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );

		Order order1 = new Order();
		customer.addOrder( order1 );

		invalidConstraints = validator.validate( customer );
		InvalidConstraint constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		assertEquals( "Wrong message", "may not be null", constraint.getMessage() );
		assertEquals( "Wrong bean class", Order.class, constraint.getBeanClass() );
		assertEquals( "Wrong root entity", customer, constraint.getRootBean() );
		assertEquals( "Wrong value", order1.getOrderNumber(), constraint.getValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraint.getPropertyPath() );

	}

	/**
	 * JSR 303: Multi-valued constraints (2.2)
	 */
	@Test
	public void testMultiValueConstraint() {
		Validator<Engine> validator = new ValidatorImpl<Engine>( Engine.class );

		Engine engine = new Engine();
		engine.setSerialNumber( "mail@foobar.com" );
		Set<InvalidConstraint<Engine>> invalidConstraints = validator.validate( engine );
		assertEquals( "Wrong number of constraints", 2, invalidConstraints.size() );

		engine.setSerialNumber( "ABCDEFGH1234" );
		invalidConstraints = validator.validate( engine );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );

		engine.setSerialNumber( "ABCD-EFGH-1234" );
		invalidConstraints = validator.validate( engine );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
	}


	/**
	 * JSR 303: Object graph validation (3.5.1)
	 */
	@Test
	public void testGraphValidation() {
		Actor clint = new Actor( "Clint", "Eastwood" );
		Actor morgan = new Actor( "Morgan", "" );
		Actor charlie = new Actor( "Charlie", "Sheen" );

		clint.addPlayedWith( charlie );
		charlie.addPlayedWith( clint );
		charlie.addPlayedWith( morgan );
		morgan.addPlayedWith( charlie );
		morgan.addPlayedWith( clint );
		clint.addPlayedWith( morgan );

		Validator<Actor> validator = new ValidatorImpl<Actor>( Actor.class );
		Set<InvalidConstraint<Actor>> invalidConstraints = validator.validate( clint );
		InvalidConstraint constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		assertEquals( "Wrong message", "may not be empty", constraint.getMessage() );
		assertEquals( "Wrong bean class", Actor.class, constraint.getBeanClass() );
		assertEquals( "Wrong root entity", clint, constraint.getRootBean() );
		assertEquals( "Wrong value", morgan.getLastName(), constraint.getValue() );
		assertEquals( "Wrong propertyName", "playedWith[0].playedWith[1].lastName", constraint.getPropertyPath() );
	}

	@Test
	public void testValidateValue() {
		Validator<Customer> validator = new ValidatorImpl<Customer>( Customer.class );

		Order order = new Order();

		Set<InvalidConstraint<Customer>> invalidConstraints = validator.validateValue(
				"orderList[0].orderNumber", null
		);
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );

		InvalidConstraint constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		assertEquals( "Wrong message", "may not be null", constraint.getMessage() );
		assertEquals( "Wrong bean class", null, constraint.getBeanClass() );
		assertEquals( "Wrong root entity", null, constraint.getRootBean() );
		assertEquals( "Wrong value", order.getOrderNumber(), constraint.getValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraint.getPropertyPath() );

		invalidConstraints = validator.validateValue( "orderList[0].orderNumber", "1234" );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
	}
}
