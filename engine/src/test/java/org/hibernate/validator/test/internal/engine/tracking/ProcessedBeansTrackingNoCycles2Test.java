/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.util.ArrayList;
import java.util.List;

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
public class ProcessedBeansTrackingNoCycles2Test {

	@Test
	public void testSerializeHibernateEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		final Parent parent = new Parent();
		parent.property = "parent property";
		final Child child = new Child();
		child.property = "child property";
		parent.children = new ArrayList<>();
		parent.children.add( child );
		parent.children.add( child );
		validator.validate( parent );
	}

	private static class Parent {

		@NotNull
		private String property;

		private List<@Valid Child> children;
	}

	private static class Child {

		@NotNull
		private String property;
	}
}