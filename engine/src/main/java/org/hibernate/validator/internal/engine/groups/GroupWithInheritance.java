/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import java.util.Iterator;
import java.util.Set;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Represents a validation group and all the groups it extends ("group inheritance").
 *
 * @author Gunnar Morling
 */
public class GroupWithInheritance implements Iterable<Group> {

	@Immutable
	private final Set<Group> groups;

	public GroupWithInheritance(Set<Group> groups) {
		this.groups = CollectionHelper.toImmutableSet( groups );
	}

	@Override
	public Iterator<Group> iterator() {
		return groups.iterator();
	}
}
