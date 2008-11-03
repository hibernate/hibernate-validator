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

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
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
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.eg.Unconstraint;

/**
 * Tests for the implementation of <code>Validator</code>.
 *
 * @author Hardy Ferentschik
 */
public class ValidatorImplTest {


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


	@Test(expected = IllegalArgumentException.class)
	public void testNullParamterToValidatorImplConstructor() {
		new ValidatorImpl<Unconstraint>( null );
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

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, "first", "second", "last" );
		assertEquals( "Wrong number of constraints", 3, constraintViolations.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, "first", "second", "last" );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be empty", constraintViolation.getMessage() );
		assertEquals( "Wrong bean class", Book.class, constraintViolation.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "title", constraintViolation.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, "first", "second", "last" );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "length must be between 0 and 30", constraintViolation.getMessage() );
		assertEquals( "Wrong bean class", Book.class, constraintViolation.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getSubtitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "subtitle", constraintViolation.getPropertyPath() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, "first", "second", "last" );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "length must be between 0 and 20", constraintViolation.getMessage() );
		assertEquals( "Wrong bean class", Author.class, constraintViolation.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", author.getCompany(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "author.company", constraintViolation.getPropertyPath() );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, "first", "second", "last" );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testDefaultGroupSequence() {
		Validator<Book> validator = new ValidatorImpl<Book>( Book.class );

		Author author = new Author();
		author.setLastName( "" );
		author.setFirstName( "" );
		Book book = new Book();
		book.setAuthor( author );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 2, constraintViolations.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, "default" );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
		assertEquals( "Wrong bean class", Book.class, constraintViolation.getBeanClass() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "title", constraintViolation.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, "default" );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testBasicValidation() {
		Validator<Customer> validator = new ValidatorImpl<Customer>( Customer.class );

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		customer.setLastName( "Doe" );

		constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
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

		Set<ConstraintViolation<Dictonary>> constraintViolations = validator.validate( dictonary, "default-alias" );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testValidationFailureInMultipleGroups() {
		Validator<Animal> validator = new ValidatorImpl<Animal>( Animal.class );
		Animal elepfant = new Animal();
		elepfant.setName( "" );
		elepfant.setDomain( Animal.Domain.EUKARYOTA );

		Set<ConstraintViolation<Animal>> constraintViolations = validator.validate( elepfant, "first", "second" );
		assertEquals(
				"The should be two invalid constraints since the same propertyName gets validated in both groups",
				1,
				constraintViolations.size()
		);

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		Set<String> expected = new HashSet<String>();
		expected.add( "first" );
		expected.add( "second" );
		assertEquals(
				"The constraint should be invalid for both groups",
				expected,
				constraintViolation.getGroups()
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

		constraintViolations = validator.validateValue( "city", "Paris" );
		assertEquals(
				"Paris should be a valid city name.",
				0,
				constraintViolations.size()
		);
	}

	@Test
	public void testValidateList() {
		Validator<Customer> validator = new ValidatorImpl<Customer>( Customer.class );

		Customer customer = new Customer();
		customer.setFirstName( "John" );
		customer.setLastName( "Doe" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );

		Order order1 = new Order();
		customer.addOrder( order1 );

		constraintViolations = validator.validate( customer );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
		assertEquals( "Wrong bean class", Order.class, constraintViolation.getBeanClass() );
		assertEquals( "Wrong root entity", customer, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", order1.getOrderNumber(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraintViolation.getPropertyPath() );

	}

	/**
	 * JSR 303: Multi-valued constraints (2.2)
	 */
	@Test
	public void testMultiValueConstraint() {
		Validator<Engine> validator = new ValidatorImpl<Engine>( Engine.class );

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
		Set<ConstraintViolation<Actor>> constraintViolations = validator.validate( clint );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be empty", constraintViolation.getMessage() );
		assertEquals( "Wrong bean class", Actor.class, constraintViolation.getBeanClass() );
		assertEquals( "Wrong root entity", clint, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", morgan.getLastName(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "playedWith[0].playedWith[1].lastName", constraintViolation.getPropertyPath() );
	}

	@Test
	public void testValidateValue() {
		Validator<Customer> validator = new ValidatorImpl<Customer>( Customer.class );

		Order order = new Order();

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validateValue(
				"orderList[0].orderNumber", null
		);
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
		assertEquals( "Wrong bean class", null, constraintViolation.getBeanClass() );
		assertEquals( "Wrong root entity", null, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", order.getOrderNumber(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraintViolation.getPropertyPath() );

		constraintViolations = validator.validateValue( "orderList[0].orderNumber", "1234" );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}
}
