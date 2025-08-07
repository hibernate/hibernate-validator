/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.performance.simple;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

	@State(Scope.Benchmark)
	public static class BenchmarkState {
		Validator validator;
		User validUser;
		User invalidUser;

		@Setup
		public void setup() {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			this.validator = factory.getValidator();
			this.validUser = new User( "John Doe", "john.doe@example.com", 25 );
			this.invalidUser = new User( "Jo", "invalid-email", 19 );
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
}
