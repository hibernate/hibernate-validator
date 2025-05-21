/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * This is not a real test, just an illustration.
 * <p>
 * In this case, given we will have all the subclasses of Child in the metadata, we should be able to know there are no
 * cycles.
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingNoCycles3Test {

	@Test
	public void testSerializeHibernateEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		validator.validate( new Parent() );
	}

	private static class Parent {

		@NotNull
		private String property;

		private List<@Valid ? extends Child> children;
	}

	private static class Child {

		@NotNull
		private String property;
	}
}
