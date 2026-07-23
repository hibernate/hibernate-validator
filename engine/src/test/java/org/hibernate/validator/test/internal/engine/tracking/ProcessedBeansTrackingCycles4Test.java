/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;

import java.util.List;
import java.util.Map;
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
 * Simple enough but this time the cascading annotation is deep in a container element with a bound.
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingCycles4Test {

	private static Stream<Arguments> createValidators() {
		return Stream.of(
				Arguments.of( ValidatorUtil.getValidator() ),
				Arguments.of( ValidatorUtil.getPredefinedValidator( Set.of( Parent.class, Child.class, ParentSuper.class ) ) )
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void testCycle(Validator validator) throws Exception {
		Parent parent = new Parent();
		parent.property = "";
		Child child = new Child();
		child.property = "";
		child.parent = parent;
		parent.children = Map.of( "1", List.of( child ) );
		assertThat( validator.validate( parent ) ).isEmpty();

		ParentSuper parent2 = new ParentSuper();
		parent2.property = "";
		Child child2 = new Child();
		child2.property = "";
		child2.parent = parent2;
		parent2.children = Map.of( "1", List.of( child2 ) );
		assertThat( validator.validate( parent2 ) ).isEmpty();
	}

	private static class Parent extends BaseParent {

		Map<String, List<@Valid ? extends Child>> children;
	}

	private static class ParentSuper extends BaseParent {


		Map<String, List<@Valid ? super Child>> children;
	}

	private static class BaseParent {

		@NotNull
		String property;

	}

	private static class Child {

		@NotNull
		String property;

		@Valid
		BaseParent parent;
	}

}
