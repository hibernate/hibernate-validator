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
import javax.validation.BeanDescriptor;
import javax.validation.ConstraintViolation;
import javax.validation.PropertyDescriptor;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

import org.hibernate.validation.engine.DefaultAlias;
import org.hibernate.validation.engine.First;
import org.hibernate.validation.engine.Last;
import org.hibernate.validation.engine.Second;
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
		assertEquals( constraintViolations.size(), 3, "Wrong number of constraints" );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		assertEquals( "may not be empty", constraintViolation.getMessage(), "Wrong message" );
		assertEquals( constraintViolation.getRootBean(), book, "Wrong root entity" );
		assertEquals( constraintViolation.getInvalidValue(), book.getTitle(), "Wrong value" );
		assertEquals( "title", constraintViolation.getPropertyPath(), "Wrong propertyName" );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "length must be between 0 and 30", constraintViolation.getMessage(), "Wrong message" );
		assertEquals( constraintViolation.getRootBean(), book, "Wrong root entity" );
		assertEquals( constraintViolation.getInvalidValue(), book.getSubtitle(), "Wrong value" );
		assertEquals( "subtitle", constraintViolation.getPropertyPath(), "Wrong propertyName" );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		assertEquals( "length must be between 0 and 20", constraintViolation.getMessage() );
		assertEquals( constraintViolation.getRootBean(), book, "Wrong root entity" );
		assertEquals( constraintViolation.getInvalidValue(), author.getCompany(), "Wrong value" );
		assertEquals( "author.company", constraintViolation.getPropertyPath(), "Wrong propertyName" );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
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
		assertEquals( constraintViolations.size(), 2, "Wrong number of constraints" );

		author.setFirstName( "Gavin" );
		author.setLastName( "King" );

		constraintViolations = validator.validate( book, Book.All.class );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		assertEquals( "may not be null", constraintViolation.getMessage(), "Wrong message" );
		assertEquals( constraintViolation.getRootBean(), book, "Wrong root entity" );
		assertEquals( constraintViolation.getInvalidValue(), book.getTitle(), "Wrong value" );
		assertEquals( "title", constraintViolation.getPropertyPath(), "Wrong propertyName" );

		book.setTitle( "Hibernate Persistence with JPA" );
		book.setSubtitle( "Revised Edition of Hibernate in Action" );

		constraintViolations = validator.validate( book, Book.All.class );
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );

		book.setSubtitle( "Revised Edition" );
		author.setCompany( "JBoss a divison of RedHat" );

		constraintViolations = validator.validate( book, Book.All.class );
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );

		author.setCompany( "JBoss" );

		constraintViolations = validator.validate( book, Book.All.class );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
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
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
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
				constraintViolations.size(),
				1,
				"The should be two invalid constraints since the same propertyName gets validated in both groups"
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
				constraintViolations.size(),
				2,
				"There should be two violations against the implicit default group"
		);

		constraintViolations = validator.validate( user, Default.class );
		assertEquals(
				constraintViolations.size(),
				2,
				"There should be two violations against the explicit defualt group"
		);

		constraintViolations = validator.validate( user, Billable.class );
		assertEquals(
				constraintViolations.size(),
				1,
				"There should be one violation against Billable"
		);

		constraintViolations = validator.validate( user, Default.class, Billable.class );
		assertEquals(
				constraintViolations.size(),
				3,
				"There should be 3 violation against Default and  Billable"
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class );
		assertEquals(
				constraintViolations.size(),
				3,
				"Three violations expected since BuyInOneClick extends Default and Billable"
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class, Billable.class );
		assertEquals(
				constraintViolations.size(),
				3,
				"BuyInOneClick already contains all other groups. Adding Billable does not change the number of violations"
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class, Default.class );
		assertEquals(
				constraintViolations.size(),
				3,
				"BuyInOneClick already contains all other groups. Adding Default does not change the number of violations"
		);

		constraintViolations = validator.validate( user, BuyInOneClick.class, Default.class, Billable.class );
		assertEquals(
				constraintViolations.size(),
				3,
				"BuyInOneClick already contains all other groups. Adding Billable and Default does not change the number of violations"
		);

		constraintViolations = validator.validate( user, Billable.class, Billable.class );
		assertEquals(
				constraintViolations.size(),
				1,
				"Adding the same group twice is still only leads to a single violation"
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
				constraintViolations.size(),
				2,
				"There should be two violations against the implicit default group"
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
				constraintViolations.size(),
				1,
				"There should only be one violation for zipcode"
		);

		ConstraintViolation<Address> violation = constraintViolations.iterator().next();
		assertConstraintViolation( violation, "may not be null", address.getClass(), null, "zipcode" );

		address.setZipcode( "41841" );

		// now the second group in the re-defined default group causes an error
		constraintViolations = validator.validate( address );
		assertEquals(
				constraintViolations.size(),
				1,
				"There should only be one violation for zipcode"
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
				constraintViolations.size(),
				1,
				"There should be one violations due to the re-defintion of the default group"
		);
		assertEquals(
				"length must be between 2 and 20",
				constraintViolations.iterator().next().getMessage(),
				"Wrong constraint"
		);

		constraintViolations = validator.validateProperty( car, "type" );
		assertEquals(
				constraintViolations.size(),
				1,
				"There should be one violations due to the re-defintion of the default group"
		);
		assertEquals(
				"length must be between 2 and 20",
				constraintViolations.iterator().next().getMessage(),
				"Wrong constraint"
		);

		constraintViolations = validator.validateValue( Car.class, "type", "A" );
		assertEquals(
				constraintViolations.size(),
				1,
				"There should be one violations due to the re-defintion of the default group"
		);
		assertEquals(
				"length must be between 2 and 20",
				constraintViolations.iterator().next().getMessage(),
				"Wrong constraint"
		);
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
					"'Default.class' cannot appear in default group sequence list.", e.getMessage(), "Wrong message"
			);
		}
	}

	/**
	 * HV-115
	 */
	@Test
	public void testImplicitGroup() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		assertTrue( beanDescriptor.isBeanConstrained() );

		Set<PropertyDescriptor> constraintProperties = beanDescriptor.getConstrainedProperties();
		assertTrue( constraintProperties.size() == 5, "Each of the properties should have at least one constraint." );

		Order order = new Order();
		Set<ConstraintViolation<Order>> violations = validator.validate( order );
		assertTrue( violations.size() == 5, "All 5 NotNull constraints should fail." );

		// use implicit group Auditable
		violations = validator.validate( order, Auditable.class );
		assertTrue( violations.size() == 4, "All 4 NotNull constraints on Auditable should fail." );
	}
}