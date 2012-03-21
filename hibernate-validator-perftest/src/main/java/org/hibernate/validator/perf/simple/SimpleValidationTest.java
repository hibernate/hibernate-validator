package org.hibernate.validator.perf.simple;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class SimpleValidationTest {
	private Validator validator;

	@Before
	public void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	public void testSimpleBeanValidation() {
		Driver driver = new Driver( "John Doe", 20 );
		Set<ConstraintViolation<Driver>> violations = validator.validate( driver );
		assertEquals( 0, violations.size() );
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



