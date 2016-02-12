/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Models a group sequence.
 *
 * @author Hardy Ferentschik
 */
public class Sequence implements Iterable<GroupWithInheritance> {

	/**
	 * An "anonymous" sequence with just a single contained element, {@code Default.class}.
	 */
	public static Sequence DEFAULT = new Sequence();

	private static final Log log = LoggerFactory.make();

	private final Class<?> sequence;
	private List<Group> groups;
	private List<GroupWithInheritance> expandedGroups;

	private Sequence() {
		this.sequence = Default.class;
		this.groups = Collections.singletonList( Group.DEFAULT_GROUP );
		this.expandedGroups = Collections.singletonList(
				new GroupWithInheritance( Collections.singleton( Group.DEFAULT_GROUP ) )
		);
	}

	public Sequence(Class<?> sequence, List<Group> groups) {
		this.groups = groups;
		this.sequence = sequence;
	}

	public List<Group> getComposingGroups() {
		return groups;
	}

	public Class<?> getDefiningClass() {
		return sequence;
	}

	public void expandInheritedGroups() {
		if ( expandedGroups != null ) {
			return;
		}

		expandedGroups = new ArrayList<GroupWithInheritance>();
		ArrayList<Group> tmpGroups = new ArrayList<Group>();

		for ( Group group : groups ) {
			HashSet<Group> groupsOfGroup = new HashSet<Group>();

			groupsOfGroup.add( group );
			addInheritedGroups( group, groupsOfGroup );

			expandedGroups.add( new GroupWithInheritance( groupsOfGroup ) );
			tmpGroups.addAll( groupsOfGroup );
		}

		groups = tmpGroups;
	}

	@Override
	public Iterator<GroupWithInheritance> iterator() {
		return expandedGroups.iterator();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Sequence sequence1 = (Sequence) o;

		if ( groups != null ? !groups.equals( sequence1.groups ) : sequence1.groups != null ) {
			return false;
		}
		if ( sequence != null ? !sequence.equals( sequence1.sequence ) : sequence1.sequence != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = sequence != null ? sequence.hashCode() : 0;
		result = 31 * result + ( groups != null ? groups.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "Sequence" );
		sb.append( "{sequence=" ).append( sequence );
		sb.append( ", groups=" ).append( groups );
		sb.append( '}' );
		return sb.toString();
	}

	/**
	 * Recursively add inherited (groups defined on superclasses).
	 *
	 * @param group the group for which the inherited groups need to be added to {@code expandedGroups}
	 * @param expandedGroups The list into which to add all groups
	 */
	private void addInheritedGroups(Group group, Set<Group> expandedGroups) {
		for ( Class<?> inheritedGroup : group.getDefiningClass().getInterfaces() ) {
			if ( isGroupSequence( inheritedGroup ) ) {
				throw log.getSequenceDefinitionsNotAllowedException();
			}
			Group g = new Group( inheritedGroup );
			expandedGroups.add( g );
			addInheritedGroups( g, expandedGroups );
		}
	}

	private boolean isGroupSequence(Class<?> clazz) {
		return clazz.getAnnotation( GroupSequence.class ) != null;
	}
}
