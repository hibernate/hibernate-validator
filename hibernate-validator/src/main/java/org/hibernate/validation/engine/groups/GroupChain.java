// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.groups;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An instance of <code>GroupExecutionChain</code> defines the order in to validate groups during the validation process.
 *
 * @author Hardy Ferentschik
 */
public class GroupChain {

	/**
	 * The list of single groups.
	 */
	private List<Group> groupList = new ArrayList<Group>();

	/**
	 * The list of sequences.
	 */
	private List<List<Group>> sequenceList = new ArrayList<List<Group>>();

	public Iterator<Group> getGroupIterator() {
		return groupList.iterator();
	}

	public Iterator<List<Group>> getSequenceIterator() {
		return sequenceList.iterator();
	}

	public boolean containsSequence(Class<?> groupSequence) {
		boolean result = false;
		for ( List<Group> sequence : sequenceList ) {
			if ( sequence.get( 0 ).getSequence().getName().equals( groupSequence.getName() ) ) {
				result = true;
				break;
			}
		}
		return result;
	}

	void insertGroup(Group group) {
		if ( !groupList.contains( group ) ) {
			groupList.add( group );
		}
	}

	void insertSequence(List<Group> groups) {
		if ( groups == null || groups.size() == 0 ) {
			return;
		}

		if ( !sequenceList.contains( groups ) ) {
			sequenceList.add( groups );
		}
	}
}