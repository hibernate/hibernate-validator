/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.performance.simple;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class SimpleValidationTest {
	private static final String[] names = {
			null,
			"Jacob",
			"Isabella",
			"Ethan",
			"Sophia",
			"Michael",
			"Emma",
			"Jayden",
			"Olivia",
			"William"
	};
	private ValidatorFactory factory;
	private Validator validator;
	private Random random;

	@Before
	public void setUp() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		random = new Random();
	}

	@Test
	public void testSimpleBeanValidation() {
		int expectedViolationCount = 0;

		String name = names[random.nextInt( 10 )];
		if ( name == null ) {
			expectedViolationCount++;
		}

		int randomAge = random.nextInt( 100 );
		if ( randomAge < 18 ) {
			expectedViolationCount++;
		}

		Driver driver = new Driver( name, randomAge );
		Set<ConstraintViolation<Driver>> violations = validator.validate( driver );
		assertEquals( expectedViolationCount, violations.size() );
	}

	@Test
	public void testSimpleBeanValidationRecreatingValidatorFactory() {
		int expectedViolationCount = 0;

		String name = names[random.nextInt( 10 )];
		if ( name == null ) {
			expectedViolationCount++;
		}

		int randomAge = random.nextInt( 100 );
		if ( randomAge < 18 ) {
			expectedViolationCount++;
		}

		Driver driver = new Driver( name, randomAge );
		Validator localValidator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Driver>> violations = localValidator.validate( driver );
		assertEquals( expectedViolationCount, violations.size() );
	}

	@Test
	public void testCascadedValidation() {
		Person kermit = new Person( "kermit" );
		Person piggy = new Person( "miss piggy" );
		Person gonzo = new Person( "gonzo" );

		kermit.addFriend( piggy ).addFriend( gonzo );
		piggy.addFriend( kermit ).addFriend( gonzo );
		gonzo.addFriend( kermit ).addFriend( piggy );

		Set<ConstraintViolation<Person>> violations = validator.validate( kermit );
		assertEquals( 0, violations.size() );
	}

	public class Driver {
		@NotNull
		String name;

		@Min(18)
		int age;

		public Driver(String name, int age) {
			this.name = name;
			this.age = age;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append( "Driver" );
			sb.append( "{name='" ).append( name ).append( '\'' );
			sb.append( ", age=" ).append( age );
			sb.append( '}' );
			return sb.toString();
		}
	}

	public class Person {
		@NotNull
		String name;

		@Valid
		Set<Person> friends = new HashSet<Person>();

		public Person(String name) {
			this.name = name;
		}

		public Person addFriend(Person friend) {
			friends.add( friend );
			return this;
		}
	}
}



