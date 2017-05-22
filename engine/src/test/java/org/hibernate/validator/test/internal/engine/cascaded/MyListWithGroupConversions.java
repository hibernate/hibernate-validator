/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.cascaded.LegacyValidOnContainerCascadingTest.ExtendedChecks1;
import org.hibernate.validator.test.internal.engine.cascaded.LegacyValidOnContainerCascadingTest.ExtendedChecks2;

class MyListWithGroupConversions<E> extends ArrayList<E> {

	@NotNull
	private final String listName;

	@NotNull(groups = ExtendedChecks1.class)
	private final String extended1 = null;

	@NotNull(groups = ExtendedChecks2.class)
	private final String extended2 = null;

	MyListWithGroupConversions(String listName, List<E> elements) {
		this.listName = listName;
		addAll( elements );
	}

	static class Visitor {

		@NotNull
		private final String name;

		@NotNull(groups = ExtendedChecks1.class)
		private final String extended1 = null;

		@NotNull(groups = ExtendedChecks2.class)
		private final String extended2 = null;

		private Visitor(String name) {
			this.name = name;
		}
	}
}
