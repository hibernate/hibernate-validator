/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jakarta.validation.GroupDefinitionException;

/**
 * Interface defining the methods needed to execute groups and sequences in the right order.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public interface ValidationOrder {

	Iterator<Group> getGroupIterator();

	Iterator<Sequence> getSequenceIterator();

	/**
	 * Asserts that the default group sequence of the validated bean can be expanded into the sequences which needs to
	 * be validated.
	 *
	 * @param defaultGroupSequence the default group sequence of the bean currently validated
	 *
	 * @throws GroupDefinitionException in case {@code defaultGroupSequence} cannot be expanded into one of the group sequences
	 * which need to be validated
	 */
	void assertDefaultGroupSequenceIsExpandable(List<Class<?>> defaultGroupSequence)
			throws GroupDefinitionException;

	/**
	 * A {@link org.hibernate.validator.internal.engine.groups.ValidationOrder} which contains a single group, {@code Default}.
	 */
	ValidationOrder DEFAULT_GROUP = new DefaultGroupValidationOrder();

	/**
	 * A {@link org.hibernate.validator.internal.engine.groups.ValidationOrder} which contains a single sequence which
	 * in turn contains a single group, {@code Default}.
	 */
	ValidationOrder DEFAULT_SEQUENCE = new DefaultSequenceValidationOrder();

	class DefaultSequenceValidationOrder implements ValidationOrder {

		private final List<Sequence> defaultSequences;

		private DefaultSequenceValidationOrder() {
			defaultSequences = Collections.singletonList( Sequence.DEFAULT );
		}

		@Override
		public Iterator<Group> getGroupIterator() {
			return Collections.<Group>emptyIterator();
		}

		@Override
		public Iterator<Sequence> getSequenceIterator() {
			return defaultSequences.iterator();
		}

		@Override
		public void assertDefaultGroupSequenceIsExpandable(List<Class<?>> defaultGroupSequence) throws GroupDefinitionException {
		}
	}

	class DefaultGroupValidationOrder implements ValidationOrder {

		private final List<Group> defaultGroups;

		private DefaultGroupValidationOrder() {
			defaultGroups = Collections.singletonList( Group.DEFAULT_GROUP );
		}

		@Override
		public Iterator<Group> getGroupIterator() {
			return defaultGroups.iterator();
		}

		@Override
		public Iterator<Sequence> getSequenceIterator() {
			return Collections.<Sequence>emptyIterator();
		}

		@Override
		public void assertDefaultGroupSequenceIsExpandable(List<Class<?>> defaultGroupSequence) throws GroupDefinitionException {
		}
	}
}
