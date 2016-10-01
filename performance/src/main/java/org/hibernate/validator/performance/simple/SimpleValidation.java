/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.simple;

import static org.assertj.core.api.Assertions.assertThat;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

	private static final Random random = new Random();

	@State(Scope.Benchmark)
	public static class ValidationState {
		public volatile Validator validator;

		{
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			validator = factory.getValidator();
		}

	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void testSimpleBeanValidation( ValidationState state ) {
		DriverSetup driverSetup = new DriverSetup();
		Set<ConstraintViolation<Driver>> violations = state.validator.validate( driverSetup.getDriver() );
		assertThat( violations ).hasSize( driverSetup.getExpectedViolationCount() );
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void testSimpleBeanValidationRecreatingValidatorFactory() {
		DriverSetup driverSetup = new DriverSetup();
		Validator localValidator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Driver>> violations = localValidator.validate( driverSetup.getDriver() );
		assertThat( violations ).hasSize( driverSetup.getExpectedViolationCount() );
	}

	public class Driver {
		@NotNull
		String name;

		@Min(18)
		int age;

		@AssertTrue
		private boolean hasDrivingLicense;

		public Driver( String name, int age, boolean hasDrivingLicense ) {
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

	private class DriverSetup {
		private int expectedViolationCount;
		private Driver driver;

		public DriverSetup() {
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

			driver = new Driver( name, randomAge, hasLicense );
		}

		public int getExpectedViolationCount() {
			return expectedViolationCount;
		}

		public Driver getDriver() {
			return driver;
		}
	}
}



