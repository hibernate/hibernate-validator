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
package org.hibernate.validation.engine.group;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

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

/**
 * Tests for the implementation of <code>Validator</code>.
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

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, First.class, Second.class, Last.class );
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
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
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
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );
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
}