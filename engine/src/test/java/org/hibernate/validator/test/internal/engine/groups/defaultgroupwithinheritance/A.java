/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.defaultgroupwithinheritance;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
@GroupSequence({ Max.class, A.class })
public class A {

	@NotNull(groups = Max.class)
	public String foo;

	@NotNull(groups = Min.class)
	public String bar;
}
