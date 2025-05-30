/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.performance.cascaded;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
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

import org.hibernate.validator.PredefinedScopeHibernateValidator;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class PredefinedScopeCascadedValidation {

	@State(Scope.Benchmark)
	public static class PredefinedScopeCascadedValidationState {

		public volatile Validator validator;
		public volatile Person person;

		public PredefinedScopeCascadedValidationState() {
			ValidatorFactory factory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
					.configure()
					.builtinConstraints( Collections.singleton( NotNull.class.getName() ) )
					.initializeBeanMetaData( Collections.singleton( Person.class ) )
					.buildValidatorFactory();
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
	public void testPredefinedScopeCascadedValidation(PredefinedScopeCascadedValidationState state, Blackhole bh) {
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
