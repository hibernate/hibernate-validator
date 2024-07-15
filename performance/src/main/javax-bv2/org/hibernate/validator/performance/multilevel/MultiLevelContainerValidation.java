/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.multilevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * @author Marko Bekhta
 */
public class MultiLevelContainerValidation {

	private static final int MAX_MAP_ENTRIES = 200;
	private static final int MAX_LIST_ENTRIES = 50;

	@State(Scope.Benchmark)
	public static class MultiLevelContainerState {

		volatile Validator validator;
		volatile MapContainer mapContainer = new MapContainer(
				RandomDataGenerator.prepareTestData( MAX_MAP_ENTRIES, MAX_LIST_ENTRIES )
		);

		volatile MultiMapContainer multiMapContainer = new MultiMapContainer(
				IntStream.range( 0, 3 ).mapToObj( e -> RandomDataGenerator.prepareTestDataRandomRuntime( MAX_MAP_ENTRIES, MAX_LIST_ENTRIES ) )
						.map( MapContainer::new )
						.collect( Collectors.toList() )
		);

		public MultiLevelContainerState() {
			try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
				validator = factory.getValidator();
			}
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(50)
	@Warmup(iterations = 10)
	@Measurement(iterations = 50)
	public void testMultiLevelPreGeneratedValidation(MultiLevelContainerState state, Blackhole bh) {
		Set<ConstraintViolation<MapContainer>> violations = state.validator.validate( state.mapContainer );
		bh.consume( violations );
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(50)
	@Warmup(iterations = 10)
	@Measurement(iterations = 50)
	public void testMultiLevelPreGeneratedWithRandomRuntimeContainersValidation(MultiLevelContainerState state, Blackhole bh) {
		Set<ConstraintViolation<MultiMapContainer>> violations = state.validator.validate( state.multiMapContainer );
		bh.consume( violations );
	}

	/**
	 * Model classes.
	 */

	private static class MultiMapContainer {
		private final Collection<@Valid MapContainer> mapContainers;

		private MultiMapContainer(Collection<MapContainer> mapContainers) {
			this.mapContainers = mapContainers;
		}
	}

	private static class MapContainer {

		private final Map<@NotNull Optional<@Valid Cinema>, List<@NotNull @Valid EmailAddress>> map;

		public MapContainer(Map<@NotNull Optional<@Valid Cinema>, List<@NotNull @Valid EmailAddress>> map) {
			this.map = map;
		}

		@SuppressWarnings("unused")
		public Map<Optional<Cinema>, List<EmailAddress>> getMap() {
			return map;
		}
	}

	@SuppressWarnings("unused")
	private static class Cinema {

		private final String name;

		private final Reference<@Valid Visitor> visitor;

		public Cinema(String name, Reference<Visitor> visitor) {
			this.name = name;
			this.visitor = visitor;
		}

		public static Cinema generate() {
			return new Cinema( RandomDataGenerator.randomString(), new SomeReference<>( Visitor.generate() ) );
		}
	}

	private interface Reference<T> {

		T getValue();
	}

	private static class SomeReference<T> implements Reference<T> {

		private final T value;

		public SomeReference(T value) {
			this.value = value;
		}

		@Override
		public T getValue() {
			return value;
		}
	}

	private static class Visitor {

		@NotNull
		private final String name;

		public Visitor(String name) {
			this.name = name;
		}

		public static Visitor generate() {
			return new Visitor( RandomDataGenerator.randomString() );
		}
	}

	private static class EmailAddress {

		// we use these simple constraints here for 2 reasons:
		// - @Email is not part of Bean Validation 1.x
		// - we don't want to use expensive constraints
		@Size(max = 50)
		@NotNull
		private final String email;

		public EmailAddress(String value) {
			this.email = value;
		}

		public static EmailAddress generate() {
			return new EmailAddress(
					String.format( Locale.ROOT, "%s@%s.com", RandomDataGenerator.randomString(), RandomDataGenerator.randomString() )
			);
		}

		public static List<EmailAddress> generateList(int numOfEntries, List<EmailAddress> addresses) {
			if ( numOfEntries < 0 ) {
				throw new IllegalArgumentException( "numOfEntries should be a positive number" );
			}
			for ( int i = 0; i < numOfEntries; i++ ) {
				addresses.add( generate() );
			}
			return addresses;
		}

		public static List<EmailAddress> generateList(int numOfEntries) {
			return generateList( numOfEntries, new ArrayList<>() );
		}
	}

	/**
	 * Test data generator.
	 */
	private static final class RandomDataGenerator {

		private static final Random RANDOM = new Random();

		private RandomDataGenerator() {
		}

		public static Map<Optional<Cinema>, List<EmailAddress>> prepareTestData(int maxEntries, int maxListEntries) {
			Map<Optional<Cinema>, List<EmailAddress>> map = new LinkedHashMap<>();
			for ( int i = 0; i < maxEntries; i++ ) {
				map.put( Optional.of( Cinema.generate() ), EmailAddress.generateList( maxListEntries ) );
			}
			return map;
		}

		public static Map<Optional<Cinema>, List<EmailAddress>> prepareTestDataRandomRuntime(int maxEntries, int maxListEntries) {
			Map<Optional<Cinema>, List<EmailAddress>> map = MAP_SUPPLIERS.get( RANDOM.nextInt( MAP_SUPPLIERS.size() ) ).get();
			for ( int i = 0; i < maxEntries; i++ ) {
				map.put( Optional.of( Cinema.generate() ), EmailAddress.generateList( maxListEntries, LIST_SUPPLIERS.get( RANDOM.nextInt( LIST_SUPPLIERS.size() ) ).get() ) );
			}
			return map;
		}

		public static String randomString() {
			char[] chars = new char[RANDOM.nextInt( 10 ) + 1];
			for ( int i = 0; i < chars.length; i++ ) {
				chars[i] = (char) RANDOM.nextInt();
			}
			return String.valueOf( chars );
		}

		private static final List<Supplier<Map<Optional<Cinema>, List<EmailAddress>>>> MAP_SUPPLIERS = Arrays.asList(
				LinkedHashMap::new,
				HashMap::new,
				() -> new TreeMap<>( (a, b) -> 0 ),
				ConcurrentHashMap::new,
				CopyOfMap::new
		);

		private static final List<Supplier<List<EmailAddress>>> LIST_SUPPLIERS = Arrays.asList(
				LinkedList::new,
				ArrayList::new,
				CopyOnWriteArrayList::new,
				OwnList::new
		);

		private static class CopyOfMap<K, V> extends HashMap<K, V> {
		}

		private static class OwnList<E> extends ArrayList<E> {
		}

	}
}
