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
