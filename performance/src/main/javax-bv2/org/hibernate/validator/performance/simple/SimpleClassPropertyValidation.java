/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.simple;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

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
 * @author Marko Bekhta
 */
public class SimpleClassPropertyValidation {

	@State(Scope.Benchmark)
	public static class ValidationState {
		public volatile Validator validator;
		public volatile Driver driver;

		{
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			validator = factory.getValidator();

			driver = new Driver( null, 1000, 100.0 );
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
		Set<ConstraintViolation<Driver>> violations = state.validator.validate( state.driver );
		bh.consume( violations );
	}

	@SpeedingDriver
	public static class Driver {
		@NotNull
		private final String name;

		@PositiveOrZero
		private final long totalDistance;

		@PositiveOrZero
		private final double timeDriven;

		public Driver(String name, long totalDistance, double timeDriven) {
			this.name = name;
			this.totalDistance = totalDistance;
			this.timeDriven = timeDriven;
		}
	}

	@Target({ TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { SpeedingDriverValidator.class })
	@interface SpeedingDriver {
		String message() default "message";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class SpeedingDriverValidator implements ConstraintValidator<SpeedingDriver, Driver> {

		@Override
		public boolean isValid(Driver driver, ConstraintValidatorContext context) {
			return driver.totalDistance / driver.timeDriven > 60.0;
		}
	}
}
