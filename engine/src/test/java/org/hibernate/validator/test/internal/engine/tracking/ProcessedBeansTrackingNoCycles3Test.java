/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.util.List;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

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
