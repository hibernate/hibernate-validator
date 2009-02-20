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
import java.util.List;

/**
 * An instance of <code>GroupExecutionChain</code> defines the order in to validate groups during the validation process.
 *
 * @author Hardy Ferentschik
 */
public class GroupChain {

	private List<Group> groupList = new ArrayList<Group>();

	private int nextGroupPointer = 0;


	/**
	 * @return Returns <code>true</code> if there is another group in the chain <code>false</code> otherwise.
	 */
	public boolean hasNext() {
		return nextGroupPointer < groupList.size();
	}

	/**
	 * @return Returns the next group in the chain or <code>null</code> if there is none.
	 */
	public Group next() {
		if ( hasNext() ) {
			return groupList.get( nextGroupPointer++ );
		}
		else {
			return null;
		}
	}

	/**
	 * @return The number of groups in this chain.
	 */
	public int size() {
		return groupList.size();
	}

	public boolean containsSequence(Class<?> groupSequence) {
		boolean result = false;
		for ( Group group : groupList ) {
			if ( groupSequence.getName().equals( group.getSequence() ) ) {
				result = true;
				break;
			}
		}
		return result;
	}

	void insertGroup(Group group) {
		if ( nextGroupPointer != 0 ) {
			throw new RuntimeException( "Trying to modify the GroupChain while iterating." );
		}

		if ( !groupList.contains( group ) ) {
			groupList.add( group );
		}
	}

	void insertSequence(List<Group> groups) {
		if ( groups == null || groups.size() == 0 ) {
			return;
		}

		if ( !containsSequence( groups.get( 0 ).getSequence() ) ) {
			groupList.addAll( groups );
		}
	}

	@Override
	public String toString() {
		return "GroupChain{" +
				"groupList=" + groupList +
				", nextGroupPointer=" + nextGroupPointer +
				'}';
	}
}