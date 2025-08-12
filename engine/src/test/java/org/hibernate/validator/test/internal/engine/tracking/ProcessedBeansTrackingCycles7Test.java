/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.testutils.ValidatorUtil;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * In this case we have @Valid placed on a container instead of the type argument.
 */
public class ProcessedBeansTrackingCycles7Test {

	@DataProvider(name = "validators")
	public Object[][] createValidators() {
		return new Object[][] {
				{ ValidatorUtil.getValidator() },
				{ ValidatorUtil.getPredefinedValidator( Set.of( Parent.class, ChildWithNoCycles.class, Child.class ) ) }
		};
	}

	@Test(dataProvider = "validators")
	public void testCycle(Validator validator) {
		Parent<ChildWithNoCycles, ChildWithNoCycles> parent = new Parent<>();
		parent.children = List.of( new ChildWithNoCycles(), new Child( parent ) );
		parent.children2 = List.of( new ChildWithNoCycles(), new Child( parent ) );
		parent.children3 = List.of( new ChildWithNoCycles(), new Child( parent ) );

		assertThat( validator.validate( parent ) ).isNotEmpty();

		Parent<String, ChildWithNoCycles> parent2 = new Parent<>();
		parent2.children = List.of( "foo", "bar" );
		parent2.children2 = List.of( new ChildWithNoCycles(), new Child( parent ) );
		parent2.children3 = List.of( new ChildWithNoCycles(), new Child( parent ) );

		assertThat( validator.validate( parent2 ) ).isNotEmpty();
	}

	private static class Parent<T, V extends ChildWithNoCycles> {

		@NotNull
		String property;

		@Valid
		List<T> children;
		@Valid
		List<V> children2;
		@Valid
		List<ChildWithNoCycles> children3;

	}

	private static class ChildWithNoCycles {

		@NotNull
		String property;
	}

	private static class Child extends ChildWithNoCycles {

		@Valid
		Parent<?, ?> parent;

		Child(Parent<?, ?> parent) {
			this.parent = parent;
		}
	}
}
