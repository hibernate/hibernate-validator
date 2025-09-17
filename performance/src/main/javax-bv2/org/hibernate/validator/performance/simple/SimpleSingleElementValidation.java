/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.performance.simple;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Threads(50)
@Warmup(iterations = 10)
@Measurement(iterations = 20)
public class SimpleSingleElementValidation {

	// The class to be validated
	private static class User {
		@NotNull
		@Size(min = 3, max = 50)
		private String name;

		@Size(min = 3, max = 50)
		@NotNull
		private String email;

		@Min(value = 20)
		private int age;

		public User(String name, String email, int age) {
			this.name = name;
			this.email = email;
			this.age = age;
		}
	}

	private static class UserNoInterpolation {
		@NotNull(message = "must be not null")
		@Size(min = 3, max = 50, message = "size must be between 3 and 50")
		private String name;

		@Size(min = 3, max = 50, message = "size must be between 3 and 50")
		@NotNull(message = "must be not null")
		private String email;

		@Min(value = 20, message = "must be at least 20")
		private int age;

		public UserNoInterpolation(String name, String email, int age) {
			this.name = name;
			this.email = email;
			this.age = age;
		}
	}

	@State(Scope.Benchmark)
	public static class BenchmarkState {
		Validator validator;
		User validUser;
		User invalidUser;
		UserNoInterpolation invalidUserNoInterpolation;

		@Setup
		public void setup() {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			this.validator = factory.getValidator();
			this.validUser = new User( "John Doe", "john.doe@example.com", 25 );
			this.invalidUser = new User( "Jo", "invalid-email", 19 );
			this.invalidUserNoInterpolation = new UserNoInterpolation( "Jo", "invalid-email", 19 );
		}
	}

	@Benchmark
	public void validObjectValidation(BenchmarkState state, Blackhole blackhole) {
		Set<ConstraintViolation<User>> violations = state.validator.validate( state.validUser );
		blackhole.consume( violations );
	}

	@Benchmark
	public void invalidObjectValidation(BenchmarkState state, Blackhole blackhole) {
		Set<ConstraintViolation<User>> violations = state.validator.validate( state.invalidUser );
		blackhole.consume( violations );
	}

	@Benchmark
	public void invalidObjectNoInterpolationValidation(BenchmarkState state, Blackhole blackhole) {
		Set<ConstraintViolation<UserNoInterpolation>> violations = state.validator.validate( state.invalidUserNoInterpolation );
		blackhole.consume( violations );
	}
}
