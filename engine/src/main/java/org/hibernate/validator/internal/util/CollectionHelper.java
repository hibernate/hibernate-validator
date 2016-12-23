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
import java.util.Iterator;
import java.util.Map;
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
		return new ArrayList<T>( getInitialCapacityFromExpectedSize( size ) );
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

	/**
	 * Builds an {@link Iterator} for a given array. It is (un)necessarily ugly because we have to deal with array of primitives.
	 *
	 * @param object a given array
	 * @return an {@code Iterator} iterating over the array
	 */
	public static Iterator<?> iteratorFromArray(Object object) {
		Iterator<?> iterator;
		if ( Object.class.isAssignableFrom( object.getClass().getComponentType() ) ) {
			iterator = new ObjectArrayIterator( (Object[]) object );
		}
		else if ( object.getClass() == boolean[].class ) {
			iterator = new BooleanArrayIterator( (boolean[]) object );
		}
		else if ( object.getClass() == int[].class ) {
			iterator = new IntArrayIterator( (int[]) object );
		}
		else if ( object.getClass() == long[].class ) {
			iterator = new LongArrayIterator( (long[]) object );
		}
		else if ( object.getClass() == double[].class ) {
			iterator = new DoubleArrayIterator( (double[]) object );
		}
		else if ( object.getClass() == float[].class ) {
			iterator = new FloatArrayIterator( (float[]) object );
		}
		else if ( object.getClass() == byte[].class ) {
			iterator = new ByteArrayIterator( (byte[]) object );
		}
		else if ( object.getClass() == short[].class ) {
			iterator = new ShortArrayIterator( (short[]) object );
		}
		else if ( object.getClass() == char[].class ) {
			iterator = new CharArrayIterator( (char[]) object );
		}
		else {
			throw new IllegalArgumentException( "Provided object " + object + " is not a supported array type" );
		}
		return iterator;
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

	private static class ObjectArrayIterator implements Iterator<Object> {

		private Object[] values;
		private int current = 0;

		private ObjectArrayIterator(Object[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Object next() {
			Object result = values[current];
			current++;
			return result;
		}
	}

	private static class BooleanArrayIterator implements Iterator<Boolean> {

		private boolean[] values;
		private int current = 0;

		private BooleanArrayIterator(boolean[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Boolean next() {
			boolean result = values[current];
			current++;
			return result;
		}
	}

	private static class IntArrayIterator implements Iterator<Integer> {

		private int[] values;
		private int current = 0;

		private IntArrayIterator(int[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Integer next() {
			int result = values[current];
			current++;
			return result;
		}
	}

	private static class LongArrayIterator implements Iterator<Long> {

		private long[] values;
		private int current = 0;

		private LongArrayIterator(long[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Long next() {
			long result = values[current];
			current++;
			return result;
		}
	}

	private static class DoubleArrayIterator implements Iterator<Double> {

		private double[] values;
		private int current = 0;

		private DoubleArrayIterator(double[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Double next() {
			double result = values[current];
			current++;
			return result;
		}
	}

	private static class FloatArrayIterator implements Iterator<Float> {

		private float[] values;
		private int current = 0;

		private FloatArrayIterator(float[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Float next() {
			float result = values[current];
			current++;
			return result;
		}
	}

	private static class ByteArrayIterator implements Iterator<Byte> {

		private byte[] values;
		private int current = 0;

		private ByteArrayIterator(byte[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Byte next() {
			byte result = values[current];
			current++;
			return result;
		}
	}

	private static class ShortArrayIterator implements Iterator<Short> {

		private short[] values;
		private int current = 0;

		private ShortArrayIterator(short[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Short next() {
			short result = values[current];
			current++;
			return result;
		}
	}

	private static class CharArrayIterator implements Iterator<Character> {

		private char[] values;
		private int current = 0;

		private CharArrayIterator(char[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return current < values.length;
		}

		@Override
		public Character next() {
			char result = values[current];
			current++;
			return result;
		}
	}
}
