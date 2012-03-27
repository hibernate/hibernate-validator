/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.validation.GroupDefinitionException;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * An instance of {@code ValidationOrder} defines the group order during one validation call.
 *
 * @author Hardy Ferentschik
 */
public final class DefaultValidationOrder implements ValidationOrder {
	private static final Log log = LoggerFactory.make();

	/**
	 * The list of single groups to be used this validation.
	 */
	private List<Group> groupList = newArrayList();

	/**
	 * The different sequences for this validation. The map contains the sequences mapped to their sequence
	 * name.
	 */
	private Map<Class<?>, Sequence> sequenceMap = newHashMap();

	public Iterator<Group> getGroupIterator() {
		return groupList.iterator();
	}

	public Iterator<Sequence> getSequenceIterator() {
		return sequenceMap.values().iterator();
	}

	public void insertGroup(Group group) {
		if ( !groupList.contains( group ) ) {
			groupList.add( group );
		}
	}

	public void insertSequence(Sequence sequence) {
		if ( sequence == null ) {
			return;
		}

		if ( !sequenceMap.containsKey( sequence.getDefiningClass() ) ) {
			sequenceMap.put( sequence.getDefiningClass(), sequence );
		}
	}

	@Override
	public String toString() {
		return "ValidationOrder{" +
				"groupList=" + groupList +
				", sequenceMap=" + sequenceMap +
				'}';
	}

	/**
	 * Asserts that the default group sequence of the validated bean can be expanded into the sequences which needs to
	 * be validated.
	 *
	 * @param defaultGroupSequence the default group sequence of the bean currently validated
	 *
	 * @throws javax.validation.GroupDefinitionException in case {@code defaultGroupSequence} cannot be expanded into one of the group sequences
	 * which need to be validated
	 */
	public void assertDefaultGroupSequenceIsExpandable(List<Class<?>> defaultGroupSequence)
			throws GroupDefinitionException {
		for ( Map.Entry<Class<?>, Sequence> entry : sequenceMap.entrySet() ) {
			List<Group> sequenceGroups = entry.getValue().getComposingGroups();
			int defaultGroupIndex = sequenceGroups.indexOf( Group.DEFAULT_GROUP );
			if ( defaultGroupIndex != -1 ) {
				List<Group> defaultGroupList = buildTempGroupList( defaultGroupSequence );
				ensureDefaultGroupSequenceIsExpandable( sequenceGroups, defaultGroupList, defaultGroupIndex );
			}
		}
	}

	private void ensureDefaultGroupSequenceIsExpandable(List<Group> groupList, List<Group> defaultGroupList, int defaultGroupIndex) {
		for ( int i = 0; i < defaultGroupList.size(); i++ ) {
			Group group = defaultGroupList.get( i );
			if ( Group.DEFAULT_GROUP.equals( group ) ) {
				continue; // we don't have to consider the default group since it is the one we want to replace
			}
			int index = groupList.indexOf( group ); // check whether the sequence contains group of the default group sequence
			if ( index == -1 ) {
				continue; // if the group is not in the sequence we can continue
			}

			if ( ( i == 0 && index == defaultGroupIndex - 1 ) || ( i == defaultGroupList.size() - 1 && index == defaultGroupIndex + 1 ) ) {
				// if we are at the beginning or end of he defaultGroupSequence and the matches are either directly before resp after we can continue as well,
				// since we basically have two groups
				continue;
			}
			throw log.getUnableToExpandDefaultGroupListException( defaultGroupList, groupList );
		}
	}

	private List<Group> buildTempGroupList(List<Class<?>> defaultGroupSequence) {
		List<Group> groups = new ArrayList<Group>();
		for ( Class<?> clazz : defaultGroupSequence ) {
			Group g = new Group( clazz );
			groups.add( g );
		}
		return groups;
	}
}
