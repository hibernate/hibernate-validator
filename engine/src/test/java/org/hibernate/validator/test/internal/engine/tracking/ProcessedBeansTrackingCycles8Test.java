/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.assertj.core.api.Assertions.assertThat;

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

public class ProcessedBeansTrackingCycles8Test {

	private static Stream<Arguments> createValidators() {
		return Stream.of(
				Arguments.of( ValidatorUtil.getValidator() ),
				Arguments.of( ValidatorUtil.getPredefinedValidator( Set.of( Grandparent.class, Parent.class, Child.class ) ) )
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void test(Validator validator) {
		Grandparent<Parent<Child>> grandparent = new Grandparent<>();
		grandparent.children = List.of(
				new Parent<>( List.of( new Child( grandparent ) ) ),
				new Parent<>( List.of( new Child( grandparent ) ) )
		);
		assertThat( validator.validate( grandparent ) ).isNotEmpty();
	}

	private static class Grandparent<T> {
		@Valid
		List<T> children;
	}

	private static class Parent<T> {
		@Valid
		List<T> children;

		Parent(List<T> children) {
			this.children = children;
		}
	}

	private static class Child {
		@Valid
		Grandparent<?> grandparent;
		@NotNull
		Integer age;

		Child(Grandparent<?> grandparent) {
			this.grandparent = grandparent;
		}
	}
}
