/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
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
 * This scenario is a return value and parameter cascading validation with some overrides.
 */
public class ProcessedBeansTrackingCyclesExecutable2Test {

	private static Stream<Arguments> createValidators() {
		return Stream.of(
				Arguments.of( ValidatorUtil.getValidator() ),
				Arguments.of( ValidatorUtil.getPredefinedValidator( Set.of( Parent.class, ChildWithNoCycles.class, Child.class, ParentInterface.class ) ) )
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle(Validator validator) throws Exception {
		Parent parent = new Parent();
		Method doSomething = Parent.class.getMethod( "doSomething", int.class, List.class );
		//		validator.forExecutables()
		//				.validateReturnValue(
		//						parent,
		//						doSomething,
		//						Optional.of( parent )
		//				);
		validator.forExecutables().validateParameters(
				parent,
				doSomething,
				new Object[] { 10, List.of( new ChildWithNoCycles(), new Child( parent ) ) }
		);
	}

	private static class Parent implements ParentInterface {

		@NotNull
		private String property;

		private List<@Valid ChildWithNoCycles> children;

		public Parent() {
			this.property = null;
			this.children = List.of( new ChildWithNoCycles(), new Child( this ) );
		}

		public Optional<Parent> doSomething(@Positive int number, List<@Valid ChildWithNoCycles> children) {
			this.property = null;
			return Optional.of( this );
		}

		@Override
		public Optional<@Valid Parent> doSomethingElse(int number, List<ChildWithNoCycles> children) {
			return Optional.empty();
		}


	}

	private interface ParentInterface {

		Optional<@Valid Parent> doSomething(@Positive int number, List<@Valid ChildWithNoCycles> children);

		Optional<Parent> doSomethingElse(@Positive int number, List<ChildWithNoCycles> children);
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
