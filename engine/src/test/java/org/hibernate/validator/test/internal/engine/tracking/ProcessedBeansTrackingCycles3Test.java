/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * This is not a real test, just an illustration.
 * <p>
 * Simple enough but this time the cascading annotation is deep in a container element.
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingCycles3Test {

	@Test
	public void testSerializeHibernateEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		validator.validate( new Parent() );
	}

	private static class Parent {

		@NotNull
		private String property;

		private Map<String, List<@Valid Child>> children;
	}

	private static class Child {

		@NotNull
		private String property;

		@Valid
		private Parent parent;
	}
}
