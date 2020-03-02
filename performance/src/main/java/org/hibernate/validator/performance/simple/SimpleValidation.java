/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.simple;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
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
public class SimpleValidation {

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

	@State(Scope.Benchmark)
	public static class ValidationState {
		public volatile Validator validator;
		public volatile ThreadLocalRandom random;
		public volatile Driver[] drivers;

		{
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			validator = factory.getValidator();
			random = ThreadLocalRandom.current();

			drivers = new Driver[100];
			for ( int i = 0; i < 100; i++ ) {
				drivers[i] = new DriverSetup( random ).getDriver();
			}
		}

		public Driver nextDriver() {
			return drivers[random.nextInt( 100 )];
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(50)
	@Warmup(iterations = 10)
	@Measurement(iterations = 20)
	public void testSimpleBeanValidation(ValidationState state, Blackhole bh) {
		Driver driver = state.nextDriver();
		Set<ConstraintViolation<Driver>> violations = state.validator.validate( driver );
		assert driver.getExpectedViolationCount() == violations.size();
		bh.consume( violations );
	}

	public static class Driver {
		@NotNull
		private String name;

		@Min(18)
		private int age;

		@AssertTrue
		private boolean hasDrivingLicense;

		private int expectedViolationCount;

		public Driver(String name, int age, boolean hasDrivingLicense, int expectedViolationCount) {
			this.name = name;
			this.age = age;
			this.hasDrivingLicense = hasDrivingLicense;
			this.expectedViolationCount = expectedViolationCount;
		}

		public int getExpectedViolationCount() {
			return expectedViolationCount;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append( "Driver" );
			sb.append( "{name='" ).append( name ).append( '\'' );
			sb.append( ", age=" ).append( age );
			sb.append( ", hasDrivingLicense=" ).append( hasDrivingLicense );
			sb.append( '}' );
			return sb.toString();
		}
	}

	private static class DriverSetup {
		private int expectedViolationCount;
		private Driver driver;

		public DriverSetup(ThreadLocalRandom random) {
			expectedViolationCount = 0;

			String name = names[random.nextInt( 10 )];
			if ( name == null ) {
				expectedViolationCount++;
			}

			int randomAge = random.nextInt( 100 );
			if ( randomAge < 18 ) {
				expectedViolationCount++;
			}

			int rand = random.nextInt( 2 );
			boolean hasLicense = rand == 1;
			if ( !hasLicense ) {
				expectedViolationCount++;
			}

			driver = new Driver( name, randomAge, hasLicense, expectedViolationCount );
		}

		public Driver getDriver() {
			return driver;
		}
	}
}
