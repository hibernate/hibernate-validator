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
package org.hibernate.validator.util;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Set that compares object by identity rather than equality. Wraps around a <code>IdentityHashMap</code>
 *
 * @author Emmanuel Bernard
 */
public class IdentitySet implements Set {
	private Map<Object, Object> map;
	private Object CONTAINS = new Object();

	public IdentitySet() {
		this( 10 );
	}

	public IdentitySet(int size) {
		this.map = new IdentityHashMap<Object, Object>( size );
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean contains(Object o) {
		return map.containsKey( o );
	}

	public Iterator iterator() {
		return map.keySet().iterator();
	}

	public Object[] toArray() {
		return map.keySet().toArray();
	}

	public boolean add(Object o) {
		return map.put( o, CONTAINS ) == null;
	}

	public boolean remove(Object o) {
		return map.remove( o ) == CONTAINS;
	}

	public boolean addAll(Collection c) {
		boolean doThing = false;
		for ( Object o : c ) {
			doThing = doThing || add( o );
		}
		return doThing;
	}

	public void clear() {
		map.clear();
	}

	public boolean removeAll(Collection c) {
		boolean remove = false;
		for ( Object o : c ) {
			remove = remove || remove( o );
		}
		return remove;
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection c) {
		for ( Object o : c ) {
			if ( !contains( o ) ) {
				return false;
			}
		}
		return true;
	}

	public Object[] toArray(Object[] a) {
		return map.keySet().toArray( a );
	}

	@Override
	public String toString() {
		return "IdentitySet{" +
				"map=" + map +
				'}';
	}
}
