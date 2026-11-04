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
 * Simple enough but this time the cascading annotation is in the container element.
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingCycles2Test {

	private static Stream<Arguments> createValidators() {
		return Stream.of(
				Arguments.of( ValidatorUtil.getValidator() ),
				Arguments.of( ValidatorUtil.getPredefinedValidator( Set.of( Parent.class, Child.class ) ) )
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testNoCycle(Validator validator) throws Exception {
		Parent parent = new Parent();
		parent.property = "";
		assertThat( validator.validate( parent ) ).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle(Validator validator) throws Exception {
		Parent parent = new Parent();
		parent.property = "";
		Child child = new Child();
		child.property = "";
		child.parent = parent;
		parent.children = List.of( child );
		assertThat( validator.validate( parent ) ).isEmpty();
	}

	private static class Parent {

		@NotNull
		private String property;

		private List<@Valid Child> children;
	}

	private static class Child {

		@NotNull
		private String property;

		@Valid
		private Parent parent;
	}
}
