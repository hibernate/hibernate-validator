/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.validation.GroupDefinitionException;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * An instance of {@code ValidationOrder} defines the group order during one validation call.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public final class DefaultValidationOrder implements ValidationOrder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The list of single groups to be used this validation.
	 */
	private List<Group> groupList;

	/**
	 * The different sequences for this validation. The map contains the sequences mapped to their sequence
	 * name.
	 */
	private Map<Class<?>, Sequence> sequenceMap;

	@Override
	public Iterator<Group> getGroupIterator() {
		if ( groupList == null ) {
			return Collections.<Group>emptyIterator();
		}
		return groupList.iterator();
	}

	@Override
	public Iterator<Sequence> getSequenceIterator() {
		if ( sequenceMap == null ) {
			return Collections.<Sequence>emptyIterator();
		}
		return sequenceMap.values().iterator();
	}

	public void insertGroup(Group group) {
		if ( groupList == null ) {
			groupList = new ArrayList<>( 5 );
		}

		if ( !groupList.contains( group ) ) {
			groupList.add( group );
		}
	}

	public void insertSequence(Sequence sequence) {
		if ( sequence == null ) {
			return;
		}

		if ( sequenceMap == null ) {
			sequenceMap = CollectionHelper.newHashMap( 5 );
		}

		sequenceMap.putIfAbsent( sequence.getDefiningClass(), sequence );
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
	 * @throws jakarta.validation.GroupDefinitionException in case {@code defaultGroupSequence} cannot be expanded into one of the group sequences
	 * which need to be validated
	 */
	@Override
	public void assertDefaultGroupSequenceIsExpandable(List<Class<?>> defaultGroupSequence)
			throws GroupDefinitionException {
		if ( sequenceMap == null ) {
			return;
		}

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
			throw LOG.getUnableToExpandDefaultGroupListException( defaultGroupList, groupList );
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
