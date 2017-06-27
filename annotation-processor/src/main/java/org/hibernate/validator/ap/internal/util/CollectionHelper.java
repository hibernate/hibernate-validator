/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provides some methods for simplified collection instantiation.
 *
 * @author Gunnar Morling
 */
public class CollectionHelper {

	private CollectionHelper() {
		// Not allowed
	}

	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	public static <T> HashSet<T> newHashSet() {
		return new HashSet<T>();
	}

	public static <T> TreeSet<T> newTreeSet() {
		return new TreeSet<T>();
	}

	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}

	public static <T> Set<T> asSet(T... ts) {

		return new HashSet<T>( Arrays.asList( ts ) );
	}

	public static <T> Set<T> asTreeSet(T... ts) {

		return new TreeSet<T>( Arrays.asList( ts ) );
	}

}
