/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This scenario is a simple return value and parameter cascading validation.
 */
public class ProcessedBeansTrackingCyclesExecutable1Test {

	private static Stream<Arguments> createValidators() {
		return Stream.of(
				Arguments.of( ValidatorUtil.getValidator() ),
				Arguments.of( ValidatorUtil.getPredefinedValidator( Set.of( Parent.class, ChildWithNoCycles.class, Child.class ) ) )
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle1(Validator validator) throws Exception {
		Parent parent = new Parent();
		Method doSomething = Parent.class.getMethod( "doSomething", int.class, List.class );
		validator.forExecutables()
				.validateReturnValue(
						parent,
						doSomething,
						Optional.of( parent )
				);

	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle2(Validator validator) throws Exception {
		Parent parent = new Parent();
		Method doSomething = Parent.class.getMethod( "doSomething", int.class, List.class );

		validator.forExecutables().validateParameters(
				parent,
				doSomething,
				new Object[] { 10, List.of( new ChildWithNoCycles(), new Child( parent ) ) }
		);

		Method doSomethingPotentiallyCascadable = Parent.class.getMethod( "doSomethingPotentiallyCascadable" );
		validator.forExecutables().validateParameters(
				parent,
				doSomethingPotentiallyCascadable,
				new Object[0]
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle3(Validator validator) throws Exception {
		Parent parent = new Parent();

		Method doSomethingPotentiallyCascadable = Parent.class.getMethod( "doSomethingPotentiallyCascadable" );
		validator.forExecutables().validateParameters(
				parent,
				doSomethingPotentiallyCascadable,
				new Object[0]
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle4(Validator validator) throws Exception {
		Parent parent = new Parent();

		Method doSomethingNoTypeIndex = Parent.class.getMethod( "doSomethingNoTypeIndex" );
		validator.forExecutables().validateParameters(
				parent,
				doSomethingNoTypeIndex,
				new Object[0]
		);
	}

	private static class Parent {

		@NotNull
		private String property;

		private List<@Valid ChildWithNoCycles> children;

		public Parent() {
			this.property = null;
			this.children = List.of( new ChildWithNoCycles(), new Child( this ) );
		}

		public Optional<@Valid Parent> doSomething(@Positive int number, List<@Valid ChildWithNoCycles> children) {
			this.property = null;
			return Optional.of( this );
		}

		public @Valid Object doSomethingPotentiallyCascadable() {
			return null;
		}

		public @Valid OptionalInt doSomethingNoTypeIndex() {
			return OptionalInt.of( 5 );
		}
	}

	private static class ChildWithNoCycles {

		@NotNull
		private String property;
	}

	private static class Child extends ChildWithNoCycles {

		@Valid
		private Parent parent;

		public Child(Parent parent) {
			this.parent = parent;
		}
	}
}
