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
 * This one is a bit more tricky: during the validation, when cascading, we take into account the runtime type to get
 * the metadata, not the declared type.
 * <p>
 * So even if you couldn't have a cycle with the declared type, when trying to find the cycles, we need to take into
 * consideration all the subclasses too. The good news is that we are in a closed world so we have them all passed
 * to our PredefinedScopedValidatorFactoryImpl!
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingCycles5Test {

	@Test
	public void testSerializeHibernateEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		validator.validate( new Parent() );
	}

	private static class Parent {

		@NotNull
		private String property;

		private List<@Valid ChildWithNoCycles> children;
	}

	private static class ChildWithNoCycles {

		@NotNull
		private String property;
	}

	private static class Child extends ChildWithNoCycles {

		@Valid
		private Parent parent;
	}
}
