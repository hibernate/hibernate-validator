/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * Original version copied from Hibernate Core.
 *
 * Cache following a "Most Recently Used" (MRU) algorithm for maintaining a
 * bounded in-memory size; the "Least Recently Used" (LRU) entry is the first
 * available for removal from the cache.
 * <p/>
 * This implementation uses a "soft limit" to the in-memory size of the cache,
 * meaning that all cache entries are kept within a completely
 * {@link java.lang.ref.SoftReference}-based map with the most recently utilized
 * entries additionally kept in a hard-reference manner to prevent those cache
 * entries soft references from becoming enqueued by the garbage collector. Thus
 * the actual size of this cache impl can actually grow beyond the stated max
 * size bound as long as GC is not actively seeking soft references for
 * enqueuement.
 * <p/>
 * The soft-size is bounded and configurable. This allows controlling memory
 * usage which can grow out of control under some circumstances, especially when
 * very large heaps are in use. Although memory usage per se should not be a
 * problem with soft references, which are cleared when necessary, this can
 * trigger extremely slow stop-the-world GC pauses when nearing full heap usage,
 * even with CMS concurrent GC (i.e. concurrent mode failure).
 * <p/>
 * <strong>Note:</strong> This class is serializable, however all entries are
 * discarded on serialization.
 *
 * @author Steve Ebersole
 * @author Manuel Dominguez Sarmiento
 * @author Hardy Ferentschik
 */
public class SoftLimitMRUCache<K, V> implements Serializable {
	/**
	 * The default strong reference count.
	 */
	public static final int DEFAULT_STRONG_REF_COUNT = 128;

	/**
	 * The default soft reference count.
	 */
	public static final int DEFAULT_SOFT_REF_COUNT = 2048;

	private final int strongRefCount;
	private final int softRefCount;

	private transient LRUMap<K, V> strongRefCache;
	private transient LRUMap<K, SoftReference<V>> softRefCache;
	private transient ReferenceQueue<V> referenceQueue;

	/**
	 * Constructs a cache with the default settings.
	 *
	 * @see #DEFAULT_STRONG_REF_COUNT
	 * @see #DEFAULT_SOFT_REF_COUNT
	 */
	public SoftLimitMRUCache() {
		this( DEFAULT_STRONG_REF_COUNT, DEFAULT_SOFT_REF_COUNT );
	}

	/**
	 * Constructs a cache with the specified settings.
	 *
	 * @param strongRefCount the strong reference count.
	 * @param softRefCount the soft reference count.
	 *
	 * @throws IllegalArgumentException if either of the arguments is less than one, or if the strong
	 * reference count is higher than the soft reference count.
	 */
	public SoftLimitMRUCache(int strongRefCount, int softRefCount) {
		if ( strongRefCount < 1 || softRefCount < 1 ) {
			throw new IllegalArgumentException( "Reference counts must be greater than zero" );
		}
		if ( strongRefCount > softRefCount ) {
			throw new IllegalArgumentException( "Strong reference count cannot exceed soft reference count" );
		}

		this.strongRefCount = strongRefCount;
		this.softRefCount = softRefCount;
		init();
	}

	/**
	 * Gets an object from the cache.
	 *
	 * @param key the cache key.
	 *
	 * @return the stored value, or <code>null</code> if no entry exists.
	 */
	public synchronized V get(K key) {
		if ( key == null ) {
			throw new IllegalArgumentException( "Key to get cannot be null" );
		}

		clearObsoleteReferences();

		SoftReference<V> ref = softRefCache.get( key );
		if ( ref != null ) {
			V refValue = ref.get();
			if ( refValue != null ) {
				// This ensures recently used entries are strongly-reachable
				strongRefCache.put( key, refValue );
				return refValue;
			}
		}

		return null;
	}

	/**
	 * Puts a value in the cache.
	 *
	 * @param key the key.
	 * @param value the value.
	 *
	 * @return the previous value stored in the cache, if any.
	 */
	public synchronized V put(K key, V value) {
		if ( key == null || value == null ) {
			throw new NullPointerException(
					getClass().getName() + "does not support null key [" + key + "] or value [" + value + "]"
			);
		}

		clearObsoleteReferences();

		strongRefCache.put( key, value );
		SoftReference<V> ref = softRefCache.put( key, new KeyedSoftReference<K, V>( key, value, referenceQueue ) );

		return ( ref != null ) ? ref.get() : null;
	}

	/**
	 * Gets the strong reference cache size.
	 *
	 * @return the strong reference cache size.
	 */
	public synchronized int size() {
		clearObsoleteReferences();
		return strongRefCache.size();
	}

	/**
	 * Gets the soft reference cache size.
	 *
	 * @return the soft reference cache size.
	 */
	public synchronized int softSize() {
		clearObsoleteReferences();
		return softRefCache.size();
	}

	/**
	 * Clears the cache.
	 */
	public synchronized void clear() {
		strongRefCache.clear();
		softRefCache.clear();
	}

	private void init() {
		this.strongRefCache = new LRUMap<K, V>( strongRefCount );
		this.softRefCache = new LRUMap<K, SoftReference<V>>( softRefCount );
		this.referenceQueue = new ReferenceQueue<V>();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		init();
	}

	private void clearObsoleteReferences() {
		// Clear entries for soft references removed by garbage collector
		KeyedSoftReference<K, V> obsoleteRef;
		while ( ( obsoleteRef = (KeyedSoftReference<K, V>) referenceQueue.poll() ) != null ) {
			K key = obsoleteRef.getKey();
			softRefCache.remove( key );
		}
	}

	private static class KeyedSoftReference<K, V> extends SoftReference<V> {
		private final K key;

		@SuppressWarnings({ "unchecked" })
		private KeyedSoftReference(K key, V value, ReferenceQueue<V> q) {
			super( value, q );
			this.key = key;
		}

		private K getKey() {
			return key;
		}
	}
}




