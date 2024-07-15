/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.unconstrained;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

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
 * @author Guillaume Smet
 */
public class UnconstrainedBeanValidation {

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
		private volatile Driver[] drivers;

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
	public void testUnconstrainedBeanValidation(ValidationState state, Blackhole bh) {
		Set<ConstraintViolation<Driver>> violations = state.validator.validate( state.nextDriver() );
		bh.consume( violations );
	}

	public static class Driver {

		private String name;

		private int age;

		private boolean hasDrivingLicense;

		public Driver(String name, int age, boolean hasDrivingLicense) {
			this.name = name;
			this.age = age;
			this.hasDrivingLicense = hasDrivingLicense;
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

		private Driver driver;

		public DriverSetup(ThreadLocalRandom random) {
			String name = names[random.nextInt( 10 )];

			int randomAge = random.nextInt( 100 );

			int rand = random.nextInt( 2 );
			boolean hasLicense = rand == 1;

			driver = new Driver( name, randomAge, hasLicense );
		}

		public Driver getDriver() {
			return driver;
		}
	}
}
