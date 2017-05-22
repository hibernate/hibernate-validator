/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import java.util.Arrays;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

import org.hibernate.validator.test.internal.engine.cascaded.LegacyValidOnContainerCascadingTest.ExtendedChecks1;
import org.hibernate.validator.test.internal.engine.cascaded.LegacyValidOnContainerCascadingTest.ExtendedChecks2;

class ValidOnListAndOnTypeArgumentWithGroupConversions {

	@Valid
	@ConvertGroup(from = Default.class, to = ExtendedChecks1.class)
	private final MyListWithGroupConversions<@Valid @ConvertGroup(from = ExtendedChecks1.class, to = ExtendedChecks2.class) VisitorWithGroups> visitors;

	private ValidOnListAndOnTypeArgumentWithGroupConversions(MyListWithGroupConversions<VisitorWithGroups> visitors) {
		this.visitors = visitors;
	}

	static ValidOnListAndOnTypeArgumentWithGroupConversions invalid() {
		return new ValidOnListAndOnTypeArgumentWithGroupConversions( new MyListWithGroupConversions<VisitorWithGroups>( null, Arrays.asList( new VisitorWithGroups( null ) ) ) );
	}

	static class VisitorWithGroups {

		@NotNull
		private final String name;

		@NotNull(groups = ExtendedChecks1.class)
		private final String extended1 = null;

		@NotNull(groups = ExtendedChecks2.class)
		private final String extended2 = null;

		private VisitorWithGroups(String name) {
			this.name = name;
		}
	}
}
