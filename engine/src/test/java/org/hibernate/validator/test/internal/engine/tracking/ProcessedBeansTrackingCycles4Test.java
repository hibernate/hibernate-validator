/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * This is not a real test, just an illustration.
 * <p>
 * Simple enough but this time the cascading annotation is deep in a container element with a bound.
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingCycles4Test {

	@Test
	public void testSerializeHibernateEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		validator.validate( new Parent() );
	}

	private static class Parent {

		@NotNull
		private String property;

		private Map<String, List<@Valid ? extends Child>> children;
	}

	private static class Child {

		@NotNull
		private String property;

		@Valid
		private Parent parent;
	}
}
