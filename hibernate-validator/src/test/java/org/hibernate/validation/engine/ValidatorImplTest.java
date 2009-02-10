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
import javax.validation.groups.Default;

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
import org.hibernate.validation.eg.DefaultAlias;
import org.hibernate.validation.eg.Dictonary;
import org.hibernate.validation.eg.Engine;
import org.hibernate.validation.eg.EnglishDictonary;
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.eg.UnconstraintEntity;
import org.hibernate.validation.eg.groups.First;
import org.hibernate.validation.eg.groups.Last;
import org.hibernate.validation.eg.groups.Second;
import org.hibernate.validation.util.TestUtil;

/**
 * Tests for the implementation of <code>Validator</code>.
 *
 * @author Hardy Ferentschik
 */
public class ValidatorImplTest {

	// @SpecAssertion( section = "3.1" )
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
		assertTrue( "There should be no constraints", !validator.getConstraintsForClass( UnconstraintEntity.class ).hasConstraints() );
	}

	@Test
	public void testHasConstraintsAndIsBeanConstrained() {
		Validator validator = TestUtil.getValidator();
		assertTrue( "There should not be constraints", !validator.getConstraintsForClass( Customer.class ).hasConstraints() );
		assertTrue( "It should be constrainted", validator.getConstraintsForClass( Customer.class ).isBeanConstrained() );
//		assertTrue( "It should be constrainted even if it has no constraint annotations - not implemented yet", validator.getConstraintsForClass( Account.class ).isBeanConstrained() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateWithNull() {
		Validator validator = TestUtil.getValidator();
		validator.validate( null );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateWithNullProperty() {
		Validator validator = TestUtil.getValidator();
		validator.validateProperty( null, "firstName" );
	}

	@Test
	public void testGroups() {
		Validator validator = TestUtil.getValidator();

		Author author = new Author();
		author.setLastName( "" );
		author.setFirstName( "" );
		Book book = new Book();
		book.setTitle( "" );
		book.setAuthor( author );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		assertEquals( "Wrong number of constraints", 3, constraintViolations.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be empty", constraintViolation.getInterpolatedMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "title", constraintViolation.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "length must be between 0 and 30", constraintViolation.getInterpolatedMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getSubtitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "subtitle", constraintViolation.getPropertyPath() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "length must be between 0 and 20", constraintViolation.getInterpolatedMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", author.getCompany(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "author.company", constraintViolation.getPropertyPath() );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testDefaultGroupSequence() {
		Validator validator = TestUtil.getValidator();

		Author author = new Author();
		author.setLastName( "" );
		author.setFirstName( "" );
		Book book = new Book();
		book.setAuthor( author );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, Default.class );
		assertEquals( "Wrong number of constraints", 2, constraintViolations.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, Default.class );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getInterpolatedMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "title", constraintViolation.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, Default.class );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, Default.class );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, Default.class );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
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
	public void testGroupSequences() {
		Validator validator = TestUtil.getValidator();

		Dictonary dictonary = new Dictonary();
		dictonary.setTitle( "English - German" );
		Author author = new Author();
		author.setLastName( "-" );
		author.setFirstName( "-" );
		author.setCompany( "Langenscheidt Publ." );
		dictonary.setAuthor( author );

		Set<ConstraintViolation<Dictonary>> constraintViolations = validator.validate( dictonary, DefaultAlias.class );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testValidationFailureInMultipleGroups() {
		Validator validator = TestUtil.getValidator();
		Animal elepfant = new Animal();
		elepfant.setName( "" );
		elepfant.setDomain( Animal.Domain.EUKARYOTA );

		Set<ConstraintViolation<Animal>> constraintViolations = validator.validate( elepfant, First.class, Second.class );
		assertEquals(
				"The should be two invalid constraints since the same propertyName gets validated in both groups",
				1,
				constraintViolations.size()
		);

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		Set<Class<?>> expected = new HashSet<Class<?>>();
		expected.add( First.class );
		expected.add( Second.class );
		assertEquals(
				"The constraint should be invalid for both groups",
				expected,
				constraintViolation.getGroups()
		);
	}

	@Test(expected = ValidationException.class)
	public void testInvalidSequenceName() {
		Validator validator = TestUtil.getValidator();
		validator.getConstraintsForClass( EnglishDictonary.class ).hasConstraints();
	}

	@Test
	public void testValidationMethod() {
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

		Order order1 = new Order();
		customer.addOrder( order1 );

		constraintViolations = validator.validate( customer );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getInterpolatedMessage() );
		assertEquals( "Wrong root entity", customer, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", order1.getOrderNumber(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraintViolation.getPropertyPath() );

	}

	@Test
	// @SpecAssertion( section = "2.2" )
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
	// @SpecAssertion( section = "3.5.1" )
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
		assertEquals( "Wrong message", "may not be empty", constraintViolation.getInterpolatedMessage() );
		assertEquals( "Wrong root entity", clint, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", morgan.getLastName(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "playedWith[0].playedWith[1].lastName", constraintViolation.getPropertyPath() );
	}

	@Test
	public void testValidateValue() {
		Validator validator = TestUtil.getValidator();

		Order order = new Order();

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validateValue(
				Customer.class, "orderList[0].orderNumber", null
		);
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getInterpolatedMessage() );
		assertEquals( "Wrong root entity", null, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", order.getOrderNumber(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "orderList[0].orderNumber", constraintViolation.getPropertyPath() );

		constraintViolations = validator.validateValue( Customer.class, "orderList[0].orderNumber", "1234" );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}
}
