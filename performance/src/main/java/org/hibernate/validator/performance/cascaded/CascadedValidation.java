/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.cascaded;

import java.util.HashSet;
import java.util.Set;
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
import org.openjdk.jmh.infra.Blackhole;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hardy Ferentschik
 */
public class CascadedValidation {
	private static final int NUMBER_OF_VALIDATION_ITERATIONS = 1000;
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
	public void testCascadedValidation(CascadedValidationState state, Blackhole bh) {
		// TODO graphs needs to be generated and deeper
		Person kermit = new Person( "kermit" );
		Person piggy = new Person( "miss piggy" );
		Person gonzo = new Person( "gonzo" );

		kermit.addFriend( piggy ).addFriend( gonzo );
		piggy.addFriend( kermit ).addFriend( gonzo );
		gonzo.addFriend( kermit ).addFriend( piggy );

		Set<ConstraintViolation<Person>> violations = state.validator.validate( kermit );
		assertThat( violations ).hasSize( 0 );

		bh.consume( violations );
	}

/*
	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(SIZE_OF_THREAD_POOL)
	@Warmup(iterations = 10)
	@Measurement(iterations = NUMBER_OF_VALIDATION_ITERATIONS)
	public void testCascadedValidationIterative(CascadedValidationState state, Blackhole bh) throws Exception {
		for ( int i = 0; i < NUMBER_OF_VALIDATION_ITERATIONS; i++ ) {
			testCascadedValidation( state );
		}
	}
*/

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
}
