/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.inheritance;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class Try {
	@NotNull(message = "field1", groups = BaseComponent.class)
	public String field1;

	@NotNull(message = "field2", groups = Component.class)
	public String field2;

	@NotNull(message = "field3", groups = OtherComponent.class)
	public String field3;

	public interface BaseComponent {
	}

	public interface Component extends BaseComponent {
	}

	public interface OtherComponent {
	}

	@GroupSequence({ Component.class, OtherComponent.class })
	public interface GlobalCheck {
	}
}


