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
package org.hibernate.validation.engine.groups;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.hibernate.validation.eg.Animal;
import org.hibernate.validation.eg.Author;
import org.hibernate.validation.eg.Book;
import org.hibernate.validation.eg.DefaultAlias;
import org.hibernate.validation.eg.Dictonary;
import org.hibernate.validation.eg.groups.First;
import org.hibernate.validation.eg.groups.Last;
import org.hibernate.validation.eg.groups.Second;
import org.hibernate.validation.util.TestUtil;
import static org.hibernate.validation.util.TestUtil.assertConstraintViolation;

/**
 * Tests for the group and group sequence feature.
 *
 * @author Hardy Ferentschik
 */
public class GroupTest {

	@Test
	public void testGroups() {
		Validator validator = TestUtil.getValidator();

		Author author = new Author();
		author.setLastName( "" );
		author.setFirstName( "" );
		Book book = new Book();
		book.setTitle( "" );
		book.setAuthor( author );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate(
				book, First.class, Second.class, Last.class
		);
		assertEquals( "Wrong number of constraints", 3, constraintViolations.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be empty", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "title", constraintViolation.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "length must be between 0 and 30", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getSubtitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "subtitle", constraintViolation.getPropertyPath() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "length must be between 0 and 20", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", author.getCompany(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "author.company", constraintViolation.getPropertyPath() );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testGroupSequence() {
		Validator validator = TestUtil.getValidator();

		Author author = new Author();
		author.setLastName( "" );
		author.setFirstName( "" );
		Book book = new Book();
		book.setAuthor( author );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, Book.All.class );
		assertEquals( "Wrong number of constraints", 2, constraintViolations.size() );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, Book.All.class );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
		assertEquals( "Wrong root entity", book, constraintViolation.getRootBean() );
		assertEquals( "Wrong value", book.getTitle(), constraintViolation.getInvalidValue() );
		assertEquals( "Wrong propertyName", "title", constraintViolation.getPropertyPath() );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, Book.All.class );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, Book.All.class );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, Book.All.class );
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

		Set<ConstraintViolation<Animal>> constraintViolations = validator.validate(
				elepfant, First.class, Second.class
		);
		assertEquals(
				"The should be two invalid constraints since the same propertyName gets validated in both groups",
				1,
				constraintViolations.size()
		);
	}

	@Test
	public void testValidateAgainstDifferentGroups() {
		User user = new User();

		// all fields per default null. Depending on the validation groups there should be  a different amount
		// of constraint failures.
		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertEquals(
				"There should be two violations against the implicit default group",
				2,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, Default.class );
		assertEquals(
				"There should be two violations against the explicit defualt group",
				2,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, Billable.class );
		assertEquals(
				"There should be one violation against Billable",
				1,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, Default.class, Billable.class );
		assertEquals(
				"There should be 3 violation against Default and  Billable",
				3,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class );
		assertEquals(
				"Three violations expected since BuyInOneClick extends Default and Billable",
				3,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class, Billable.class );
		assertEquals(
				"BuyInOneClick already contains all other groups. Adding Billable does not change the number of violations",
				3,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class, Default.class );
		assertEquals(
				"BuyInOneClick already contains all other groups. Adding Default does not change the number of violations",
				3,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class, Default.class, Billable.class );
		assertEquals(
				"BuyInOneClick already contains all other groups. Adding Billable and Default does not change the number of violations",
				3,
				constraintViolations.size()
		);

		constraintViolations = validator.validate( user, Billable.class, Billable.class );
		assertEquals(
				"Adding the same group twice is still only leads to a single violation",
				1,
				constraintViolations.size()
		);
	}

	/**
	 * HV-85
	 */
	@Test
	public void testGroupSequenceFollowedByGroup() {
		User user = new User();
		user.setFirstname( "Foo" );
		user.setLastname( "Bar" );
		user.setPhoneNumber( "+46 123-456" );

		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<User>> constraintViolations = validator.validate(
				user, BuyInOneClick.class, Optional.class
		);
		assertEquals(
				"There should be two violations against the implicit default group",
				2,
				constraintViolations.size()
		);

		for ( ConstraintViolation<User> constraintViolation : constraintViolations ) {
			if ( constraintViolation.getPropertyPath().equals( "defaultCreditCard" ) ) {
				assertConstraintViolation(
						constraintViolation,
						"may not be null",
						User.class,
						null,
						"defaultCreditCard"
				);
			}
			else if ( constraintViolation.getPropertyPath().equals( "phoneNumber" ) ) {
				assertConstraintViolation(
						constraintViolation,
						"must match \"[0-9 -]?\"",
						User.class,
						"+46 123-456",
						"phoneNumber"
				);
			}
			else {
				fail( "Unexpected violation" );
			}
		}
	}

	/**
	 * HV-113
	 */
	@Test
	public void testRedefiningDefaultGroup() {
		Address address = new Address();
		address.setStreet( "Guldmyntgatan" );
		address.setCity( "Gothenborg" );

		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<Address>> constraintViolations = validator.validate( address );
		assertEquals(
				"There should only be one violation for zipcode",
				1,
				constraintViolations.size()
		);

		ConstraintViolation<Address> violation = constraintViolations.iterator().next();
		assertConstraintViolation( violation, "may not be null", address.getClass(), null, "zipcode" );

		address.setZipcode( "41841" );

		// now the second group in the re-defined default group causes an error
		constraintViolations = validator.validate( address );
		assertEquals(
				"There should only be one violation for zipcode",
				1,
				constraintViolations.size()
		);

		violation = constraintViolations.iterator().next();
		assertConstraintViolation( violation, "{validator.zipCodeCoherenceChecker}", address.getClass(), address, "" );
	}

	/**
	 * HV-113
	 */
	@Test
	public void testRedefiningDefaultGroup2() {
		Car car = new Car();
		car.setType( "A" );

		Validator validator = TestUtil.getValidator();

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );
		assertEquals(
				"There should be one violations due to the re-defintion of the default group",
				1,
				constraintViolations.size()
		);
		assertEquals( "Wrong constraint", "length must be between 2 and 20", constraintViolations.iterator().next().getMessage() );

		constraintViolations = validator.validateProperty( car, "type" );
		assertEquals(
				"There should be one violations due to the re-defintion of the default group",
				1,
				constraintViolations.size()
		);
		assertEquals( "Wrong constraint", "length must be between 2 and 20", constraintViolations.iterator().next().getMessage() );

		constraintViolations = validator.validateValue( Car.class, "type", "A" );
		assertEquals(
				"There should be one violations due to the re-defintion of the default group",
				1,
				constraintViolations.size()
		);
		assertEquals( "Wrong constraint", "length must be between 2 and 20", constraintViolations.iterator().next().getMessage() );
	}

	/**
	 * HV-113
	 */
	@Test
	public void testInvalidRedefinitionOfDefaultGroup() {
		Address address = new AddressWithInvalidGroupSequence();
		Validator validator = TestUtil.getValidator();
		try {
			validator.validate( address );
			fail( "It shoud not be allowed to have Default.class in the group sequence of a class." );
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong message", "'Default.class' cannot appear in default group sequence list.", e.getMessage()
			);
		}
	}
}