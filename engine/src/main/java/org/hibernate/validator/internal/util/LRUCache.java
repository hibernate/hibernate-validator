/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU cache implementation based on {@link LinkedHashMap} and it's {@code accessOrder=true} parameter.
 *
 * @author Marko Bekhta
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {

	/**
	 * Same load factor as used in {@link java.util.HashMap}
	 */
	private static final float LOAD_FACTOR = 0.75f;
	/**
	 * Same initial capacity as used in {@link java.util.HashMap}
	 */
	private static final int INITIAL_CAPACITY = 16;

	private final int maxCapacity;

	private LRUCache(int initialCapacity, int maxCapacity) {
		super( initialCapacity, LOAD_FACTOR, true );
		this.maxCapacity = maxCapacity;
	}

	@Override protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > maxCapacity;
	}

	public static <K,V> Map<K,V> getInstance(int maxCapacity) {
		return Collections.synchronizedMap( new LRUCache<>( INITIAL_CAPACITY, maxCapacity ) );
	}

	public static <K,V> Map<K,V> getInstance(int initialCapacity, int maxCapacity) {
		return Collections.synchronizedMap( new LRUCache<>( initialCapacity, maxCapacity ) );
	}
}
