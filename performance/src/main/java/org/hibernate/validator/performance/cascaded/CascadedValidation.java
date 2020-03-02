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
import java.util.concurrent.TimeUnit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

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

/**
 * @author Hardy Ferentschik
 */
public class CascadedValidation {

	@State(Scope.Benchmark)
	public static class CascadedValidationState {
		public volatile Validator validator;
		public volatile Person person;

		public CascadedValidationState() {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			validator = factory.getValidator();

			// TODO graphs needs to be generated and deeper
			Person kermit = new Person( "kermit" );
			Person piggy = new Person( "miss piggy" );
			Person gonzo = new Person( "gonzo" );

			kermit.addFriend( piggy ).addFriend( gonzo );
			piggy.addFriend( kermit ).addFriend( gonzo );
			gonzo.addFriend( kermit ).addFriend( piggy );

			person = kermit;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(50)
	@Warmup(iterations = 10)
	@Measurement(iterations = 20)
	public void testCascadedValidation(CascadedValidationState state, Blackhole bh) {
		Set<ConstraintViolation<Person>> violations = state.validator.validate( state.person );
		assertThat( violations ).hasSize( 0 );

		bh.consume( violations );
	}

	public static class Person {
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
