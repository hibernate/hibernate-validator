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
package org.hibernate.validator.internal.util;

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
public class IdentitySet implements Set<Object> {
	private final Map<Object, Object> map;
	private final Object CONTAINS = new Object();

	public IdentitySet() {
		this( 10 );
	}

	public IdentitySet(int size) {
		this.map = new IdentityHashMap<Object, Object>( size );
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey( o );
	}

	@Override
	public Iterator<Object> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public boolean add(Object o) {
		return map.put( o, CONTAINS ) == null;
	}

	@Override
	public boolean remove(Object o) {
		return map.remove( o ) == CONTAINS;
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		boolean doThing = false;
		for ( Object o : c ) {
			doThing = doThing || add( o );
		}
		return doThing;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean removeAll(Collection<? extends Object> c) {
		boolean remove = false;
		for ( Object o : c ) {
			remove = remove || remove( o );
		}
		return remove;
	}

	@Override
	public boolean retainAll(Collection<? extends Object> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<? extends Object> c) {
		for ( Object o : c ) {
			if ( !contains( o ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
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
