/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.cascaded;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hardy Ferentschik
 */
public class CascadedValidationWithManyPrimitiveValids {

	@State(Scope.Benchmark)
	public static class CascadedValidationState {

		public volatile Validator validator;
		public volatile Person person;

		public CascadedValidationState() {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			validator = factory.getValidator();

			// TODO graphs needs to be generated and deeper
			Person kermit = new Person( "kermit", 55, 0, 1_000_000_000L, 25.6f, true );
			Person piggy = new Person( "miss piggy", 19, 0, 10_000_000L, 55.6f, true );
			Person gonzo = new Person( "gonzo", 55, 1_000_000, 100_000_000L, 35.1f, true );

			for ( var i = 0; i < 10; i++ ) {
				kermit.addBloodPressureReading( i );
				piggy.addBloodPressureReading( 1000 + i );
				gonzo.addBloodPressureReading( 10000 + i );
				kermit.addBrainCellCount( (long) i );
				piggy.addBrainCellCount( (long) ( 1000 + i ) );
				gonzo.addBrainCellCount( (long) ( 10000 + i ) );
			}

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
		@Valid
		String name;

		@Valid
		int age;

		@Valid
		long hairCount;

		@Valid
		double balance;

		@Valid
		float size;

		@Valid
		boolean alive;

		@Valid
		List<Integer> bloodPressureReadings = new ArrayList<>();

		List<@Valid Long> brainCellCount = new ArrayList<>();

		@Valid
		Set<Person> friends = new HashSet<>();

		public Person(String name, int age, long hairCount, double balance, float size, boolean alive) {
			this.name = name;
			this.age = age;
			this.hairCount = hairCount;
			this.balance = balance;
			this.size = size;
			this.alive = alive;
		}

		public Person addFriend(Person friend) {
			friends.add( friend );
			return this;
		}

		public Person addBrainCellCount(Long bcCount) {
			brainCellCount.add( bcCount );
			return this;
		}

		public Person addBloodPressureReading(Integer bpReading) {
			bloodPressureReadings.add( bpReading );
			return this;
		}
	}
}
