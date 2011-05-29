/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides some methods for simplified collection instantiation.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class CollectionHelper {

	private CollectionHelper() {
	}

	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return new HashMap<K, V>( size );
	}

	public static <T> HashSet<T> newHashSet() {
		return new HashSet<T>();
	}

	public static <T> HashSet<T> newHashSet(int size) {
		return new HashSet<T>( size );
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
		return new HashSet<T>( Arrays.asList( ts ) );
	}

	/**
	 * Creates a map containing the given list's values partitioned by the given
	 * partitioner.
	 *
	 * @param <K> The key type of the resulting map.
	 * @param <V> The element type of the list to be partitioned.
	 * @param list The list to be partitioned.
	 * @param partitioner The partitioner to be used for determining the partitions.
	 *
	 * @return A map containing the given list's values partitioned by the given
	 *         partitioner.
	 */
	public static <K, V> Map<K, List<V>> partition(List<V> list, Partitioner<K, V> partitioner) {

		Map<K, List<V>> theValue = newHashMap();

		for ( V v : list ) {
			K key = partitioner.getPartition( v );

			List<V> partition = theValue.get( key );
			if ( partition == null ) {
				partition = newArrayList();
				theValue.put( key, partition );
			}

			partition.add( v );
		}

		return theValue;
	}

	public interface Partitioner<K, V> {

		K getPartition(V v);
	}
}
