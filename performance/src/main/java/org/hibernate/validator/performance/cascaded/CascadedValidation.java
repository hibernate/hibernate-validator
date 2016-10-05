/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.cascaded;

import static org.assertj.core.api.Assertions.assertThat;

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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * @author Hardy Ferentschik
 */
public class CascadedValidation {
	private static final int NUMBER_OF_RUNNABLES = 10000;
	private static final int SIZE_OF_THREAD_POOL = 50;

	@State(Scope.Benchmark)
	public static class CascadedValidationState {
		public volatile Validator validator;

		public CascadedValidationState() {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			validator = factory.getValidator();
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(50)
	@Warmup(iterations = 10)
	@Measurement(iterations = 50)
	public void testCascadedValidation(CascadedValidationState state) {
		// TODO graphs needs to be generated and deeper
		Person kermit = new Person( "kermit" );
		Person piggy = new Person( "miss piggy" );
		Person gonzo = new Person( "gonzo" );

		kermit.addFriend( piggy ).addFriend( gonzo );
		piggy.addFriend( kermit ).addFriend( gonzo );
		gonzo.addFriend( kermit ).addFriend( piggy );

		Set<ConstraintViolation<Person>> violations = state.validator.validate( kermit );
		assertThat( violations ).hasSize( 0 );
	}

//	@Benchmark
//	@BenchmarkMode(Mode.All)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	@Fork(value = 1)
//	@Threads(2)
//	@Warmup(iterations = 10)
//	@Measurement(iterations = 50)
	public void testCascadedValidationMultiThreaded(CascadedValidationState state) throws Exception {
		CountDownLatch startLatch = new CountDownLatch( 1 );
		ExecutorService executor = Executors.newFixedThreadPool( SIZE_OF_THREAD_POOL );
		for ( int i = 0; i <= NUMBER_OF_RUNNABLES; i++ ) {
			Runnable run = new TestRunner( startLatch, state );
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
		Set<Person> friends = new HashSet<>();

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
		private final CascadedValidationState state;

		public TestRunner(CountDownLatch latch, CascadedValidationState state) {
			this.latch = latch;
			this.state = state;
		}

		@Override
		public void run() {
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			for ( int i = 0; i < NUMBER_OF_VALIDATION_ITERATIONS; i++ ) {
				testCascadedValidation( state );
			}
		}
	}
}
