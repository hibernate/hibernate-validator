/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.cascaded;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class CascadedValidationTest {
	private static Validator validator;
	private static final int NUMBER_OF_RUNNABLES = 10000;
	private static final int SIZE_OF_THREAD_POOL = 50;

	@BeforeClass
	public static void setupValidatorInstance() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testCascadedValidation() {
		// TODO graphs needs to be generated and deeper
		Person kermit = new Person( "kermit" );
		Person piggy = new Person( "miss piggy" );
		Person gonzo = new Person( "gonzo" );

		kermit.addFriend( piggy ).addFriend( gonzo );
		piggy.addFriend( kermit ).addFriend( gonzo );
		gonzo.addFriend( kermit ).addFriend( piggy );

		Set<ConstraintViolation<Person>> violations = validator.validate( kermit );
		assertEquals( 0, violations.size() );
	}

	/**
	 * To be executed manually. Not part of the JMeter tests for now.
	 */
	@Test
	public void testCascadedValidationMultiThreaded() throws Exception {
		CountDownLatch startLatch = new CountDownLatch( 1 );
		ExecutorService executor = Executors.newFixedThreadPool( SIZE_OF_THREAD_POOL );
		for ( int i = 0; i <= NUMBER_OF_RUNNABLES; i++ ) {
			Runnable run = new TestRunner( startLatch );
			executor.execute( run );
		}
		executor.shutdown();
		startLatch.countDown(); //start!
		executor.awaitTermination( 600, TimeUnit.SECONDS );
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

	public class TestRunner implements Runnable {
		private static final int NUMBER_OF_VALIDATION_ITERATIONS = 1000;
		private final CountDownLatch latch;

		public TestRunner(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void run() {
			try {
				latch.await();
			}
			catch ( InterruptedException e ) {
				e.printStackTrace();
				return;
			}
			for ( int i = 0; i < NUMBER_OF_VALIDATION_ITERATIONS; i++ ) {
				testCascadedValidation();
			}
		}
	}
}
