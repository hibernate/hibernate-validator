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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides some methods for simplified collection instantiation.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public final class CollectionHelper {

	private CollectionHelper() {
	}

	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return new HashMap<K, V>( getInitialCapacityFromExpectedSize( size ) );
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
		return new HashSet<T>( getInitialCapacityFromExpectedSize( size ) );
	}

	public static <T> HashSet<T> newHashSet(Collection<? extends T> c) {
		return new HashSet<T>( c );
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

	@SafeVarargs
	public static <T> ArrayList<T> newArrayList(Iterable<T>... iterables) {
		ArrayList<T> resultList = newArrayList();
		for ( Iterable<T> oneIterable : iterables ) {
			for ( T oneElement : oneIterable ) {
				resultList.add( oneElement );
			}
		}
		return resultList;
	}

	@SafeVarargs
	public static <T> Set<T> asSet(T... ts) {
		return new HashSet<>( Arrays.asList( ts ) );
	}

	public static <T> List<T> toImmutableList(List<? extends T> list) {
		switch ( list.size() ) {
			case 0:
				return Collections.emptyList();
			case 1:
				return Collections.singletonList( list.get( 0 ) );
			default:
				return Collections.unmodifiableList( list );
		}
	}

	public static <T> Set<T> toImmutableSet(Set<? extends T> set) {
		switch ( set.size() ) {
			case 0:
				return Collections.emptySet();
			case 1:
				return Collections.singleton( set.iterator().next() );
			default:
				return Collections.unmodifiableSet( set );
		}
	}

	public static <K, V> Map<K, V> toImmutableMap(Map<K, V> map) {
		switch ( map.size() ) {
			case 0:
				return Collections.emptyMap();
			case 1:
				Entry<K, V> entry = map.entrySet().iterator().next();
				return Collections.singletonMap( entry.getKey(), entry.getValue() );
			default:
				return Collections.unmodifiableMap( map );
		}
	}

	/**
	 * As the default loadFactor is of 0.75, we need to calculate the initial capacity from the expected size to avoid
	 * resizing the collection when we populate the collection with all the initial elements. We use a calculation
	 * similar to what is done in {@link HashMap#putAll(Map)}.
	 *
	 * @param expectedSize the expected size of the collection
	 * @return the initial capacity of the collection
	 */
	private static int getInitialCapacityFromExpectedSize(int expectedSize) {
		if ( expectedSize < 3 ) {
			return expectedSize + 1;
		}
		return (int) ( (float) expectedSize / 0.75f + 1.0f );
	}

	/**
	 * Builds an {@link Iterator} for a given array. It is (un)necessarily ugly because we have to deal with array of primitives.
	 *
	 * @param object a given array
	 * @return an {@code Iterator} iterating over the array
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" }) // Reflection is used to ensure the correct types are used
	public static Iterator<?> iteratorFromArray(Object object) {
		return new ArrayIterator( accessorFromArray( object ), object );
	}

	/**
	 * Builds an {@link Iterable} for a given array. It is (un)necessarily ugly because we have to deal with array of primitives.
	 *
	 * @param object a given array
	 * @return an {@code Iterable} providing iterators over the array
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" }) // Reflection is used to ensure the correct types are used
	public static Iterable<?> iterableFromArray(Object object) {
		return new ArrayIterable( accessorFromArray( object ), object );
	}

	private static ArrayAccessor<?, ?> accessorFromArray(Object object) {
		ArrayAccessor<?, ?> accessor;
		if ( Object.class.isAssignableFrom( object.getClass().getComponentType() ) ) {
			accessor = ArrayAccessor.OBJECT;
		}
		else if ( object.getClass() == boolean[].class ) {
			accessor = ArrayAccessor.BOOLEAN;
		}
		else if ( object.getClass() == int[].class ) {
			accessor = ArrayAccessor.INT;
		}
		else if ( object.getClass() == long[].class ) {
			accessor = ArrayAccessor.LONG;
		}
		else if ( object.getClass() == double[].class ) {
			accessor = ArrayAccessor.DOUBLE;
		}
		else if ( object.getClass() == float[].class ) {
			accessor = ArrayAccessor.FLOAT;
		}
		else if ( object.getClass() == byte[].class ) {
			accessor = ArrayAccessor.BYTE;
		}
		else if ( object.getClass() == short[].class ) {
			accessor = ArrayAccessor.SHORT;
		}
		else if ( object.getClass() == char[].class ) {
			accessor = ArrayAccessor.CHAR;
		}
		else {
			throw new IllegalArgumentException( "Provided object " + object + " is not a supported array type" );
		}
		return accessor;
	}

	private static class ArrayIterable<A, T> implements Iterable<T> {
		private final ArrayAccessor<A, T> accessor;
		private final A values;

		public ArrayIterable(ArrayAccessor<A, T> accessor, A values) {
			this.accessor = accessor;
			this.values = values;
		}

		@Override
		public final Iterator<T> iterator() {
			return new ArrayIterator<>( accessor, values );
		}
	}

	private static class ArrayIterator<A, T> implements Iterator<T> {
		private final ArrayAccessor<A, T> accessor;
		private final A values;
		private int current = 0;

		public ArrayIterator(ArrayAccessor<A, T> accessor, A values) {
			this.accessor = accessor;
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < accessor.size( values );
		}

		@Override
		public T next() {
			T result = accessor.get( values, current );
			current++;
			return result;
		}
	}

	private interface ArrayAccessor<A, T> {

		int size(A array);

		T get(A array, int index);

		ArrayAccessor<Object[], Object> OBJECT = new ArrayAccessor<Object[], Object>() {
			@Override
			public int size(Object[] array) {
				return array.length;
			}

			@Override
			public Object get(Object[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<boolean[], Boolean> BOOLEAN = new ArrayAccessor<boolean[], Boolean>() {
			@Override
			public int size(boolean[] array) {
				return array.length;
			}

			@Override
			public Boolean get(boolean[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<int[], Integer> INT = new ArrayAccessor<int[], Integer>() {
			@Override
			public int size(int[] array) {
				return array.length;
			}

			@Override
			public Integer get(int[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<long[], Long> LONG = new ArrayAccessor<long[], Long>() {
			@Override
			public int size(long[] array) {
				return array.length;
			}

			@Override
			public Long get(long[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<double[], Double> DOUBLE = new ArrayAccessor<double[], Double>() {
			@Override
			public int size(double[] array) {
				return array.length;
			}

			@Override
			public Double get(double[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<float[], Float> FLOAT = new ArrayAccessor<float[], Float>() {
			@Override
			public int size(float[] array) {
				return array.length;
			}

			@Override
			public Float get(float[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<byte[], Byte> BYTE = new ArrayAccessor<byte[], Byte>() {
			@Override
			public int size(byte[] array) {
				return array.length;
			}

			@Override
			public Byte get(byte[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<short[], Short> SHORT = new ArrayAccessor<short[], Short>() {
			@Override
			public int size(short[] array) {
				return array.length;
			}

			@Override
			public Short get(short[] array, int index) {
				return array[index];
			}
		};

		ArrayAccessor<char[], Character> CHAR = new ArrayAccessor<char[], Character>() {
			@Override
			public int size(char[] array) {
				return array.length;
			}

			@Override
			public Character get(char[] array, int index) {
				return array[index];
			}
		};
	}
}
