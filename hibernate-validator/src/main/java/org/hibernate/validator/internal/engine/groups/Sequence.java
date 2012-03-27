/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.engine.groups;

import java.util.List;
import javax.validation.GroupSequence;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Models a group sequence.
 *
 * @author Hardy Ferentschik
 */
public class Sequence {
	private static final Log log = LoggerFactory.make();

	private final Class<?> sequence;
	private List<Group> groups;
	private boolean inheritedGroupsExpanded = false;

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
		if ( inheritedGroupsExpanded ) {
			return;
		}

		List<Group> expandedGroups = newArrayList();
		for ( Group group : groups ) {
			expandedGroups.add( group );
			addInheritedGroups( group, expandedGroups );
		}
		groups = expandedGroups;
		inheritedGroupsExpanded = true;
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
	private void addInheritedGroups(Group group, List<Group> expandedGroups) {
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


