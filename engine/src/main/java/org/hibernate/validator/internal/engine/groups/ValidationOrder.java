/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import java.util.Iterator;
import java.util.List;
import javax.validation.GroupDefinitionException;

/**
 * Interface defining the methods needed to execute groups and sequences in the right order.
 *
 * @author Hardy Ferentschik
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
}
