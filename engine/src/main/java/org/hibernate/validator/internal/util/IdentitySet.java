/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
