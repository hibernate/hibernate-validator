/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.tracking;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * This is not a real test, just an illustration.
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingNoCycles1Test {

	@Test
	public void testSerializeHibernateEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		validator.validate( new Parent() );
	}

	private static class Parent {

		@NotNull
		private String property;

		@Valid
		private Child child;
	}

	private static class Child {

		@NotNull
		private String property;
	}
}
