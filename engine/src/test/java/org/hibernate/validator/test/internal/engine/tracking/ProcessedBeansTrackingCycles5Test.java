/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This is not a real test, just an illustration.
 * <p>
 * This one is a bit more tricky: during the validation, when cascading, we take into account the runtime type to get
 * the metadata, not the declared type.
 * <p>
 * So even if you couldn't have a cycle with the declared type, when trying to find the cycles, we need to take into
 * consideration all the subclasses too. The good news is that we are in a closed world so we have them all passed
 * to our PredefinedScopedValidatorFactoryImpl!
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingCycles5Test {

	private static Stream<Arguments> createValidators() {
		return Stream.of(
				Arguments.of( ValidatorUtil.getValidator() ),
				Arguments.of( ValidatorUtil.getPredefinedValidator( Set.of( Parent.class, ChildWithNoCycles.class, Child.class ) ) )
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle(Validator validator) {
		assertThat( validator.validate( new Parent() ) ).isNotEmpty();
	}

	private static class Parent {

		@NotNull
		private String property;

		private List<@Valid ChildWithNoCycles> children;

		public Parent() {
			this.property = null;
			this.children = List.of( new ChildWithNoCycles(), new Child( this ) );
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
