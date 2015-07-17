/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import javax.validation.groups.Default;

/**
 * Encapsulates a single group.
 *
 * @author Hardy Ferentschik
 */
public class Group {
	public static final Group DEFAULT_GROUP = new Group( Default.class );

	/**
	 * The actual group.
	 */
	private Class<?> group;

	public Group(Class<?> group) {
		this.group = group;
	}

	public Class<?> getDefiningClass() {
		return group;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Group group1 = (Group) o;

		if ( group != null ? !group.equals( group1.group ) : group1.group != null ) {
			return false;
		}
		return true;
	}

	public boolean isDefaultGroup() {
		return getDefiningClass().getName().equals( Default.class.getName() );
	}

	@Override
	public int hashCode() {
		return group != null ? group.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "Group{" + "group=" + group.getName() + '}';
	}
}
