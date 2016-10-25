/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides some methods for simplified collection instantiation.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Hardy Ferentschik
 */
public final class CollectionHelper {

	private CollectionHelper() {
	}

	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return new HashMap<K, V>( size );
	}

	public static <K, V> HashMap<K, V> newHashMap(Map<K, V> map) {
		return new HashMap<K, V>( map );
	}

	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
		return new ConcurrentHashMap<K, V>();
	}

	public static <T> HashSet<T> newHashSet() {
		return new HashSet<T>();
	}

	public static <T> HashSet<T> newHashSet(int size) {
		return new HashSet<T>( size );
	}

	public static <T> HashSet<T> newHashSet(Collection<? extends T> c) {
		return new HashSet<T>( c );
	}

	public static <T> HashSet<T> newHashSet(Collection<? extends T> s1, Collection<? extends T> s2) {
		HashSet<T> set = CollectionHelper.<T>newHashSet( s1 );
		set.addAll( s2 );
		return set;
	}

	public static <T> HashSet<T> newHashSet(Iterable<? extends T> iterable) {
		HashSet<T> set = newHashSet();
		for ( T t : iterable ) {
			set.add( t );
		}
		return set;
	}

	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}

	public static <T> ArrayList<T> newArrayList(int size) {
		return new ArrayList<T>( size );
	}

	public static <T> ArrayList<T> newArrayList(Iterable<T>... iterables) {
		ArrayList<T> resultList = newArrayList();
		for ( Iterable<T> oneIterable : iterables ) {
			for ( T oneElement : oneIterable ) {
				resultList.add( oneElement );
			}
		}
		return resultList;
	}

	public static <T> Set<T> asSet(T... ts) {
		return new HashSet<>( Arrays.asList( ts ) );
	}
}
