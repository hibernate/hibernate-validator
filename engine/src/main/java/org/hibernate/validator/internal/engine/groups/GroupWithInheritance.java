/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a validation group and all the groups it extends ("group inheritance").
 *
 * @author Gunnar Morling
 */
public class GroupWithInheritance implements Iterable<Group> {

	private final Set<Group> groups;

	public GroupWithInheritance(Set<Group> groups) {
		this.groups = Collections.unmodifiableSet( groups );
	}

	@Override
	public Iterator<Group> iterator() {
		return groups.iterator();
	}
}
