/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ProcessedBeansTrackingCycles8Test {

	@DataProvider(name = "validators")
	public Object[][] createValidators() {
		return new Object[][] {
				{ ValidatorUtil.getValidator() },
				{ ValidatorUtil.getPredefinedValidator( Set.of( Grandparent.class, Parent.class, Child.class ) ) }
		};
	}

	@Test(dataProvider = "validators")
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
