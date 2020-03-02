/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.stringrepresentation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPathStringRepresentations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.testng.annotations.Test;

public class ContainerElementPathStringRepresentationTest extends AbstractPathStringRepresentationTest {

	@Test
	public void testMapInvalidKeyTypeArgument() {
		DemographicStatistics statistics = new DemographicStatistics();
		statistics.put( null, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "inhabitantsPerAddress<K>[].<map key>" ); // the key is null, thus the '[]'
	}

	@Test
	public void testMapInvalidKeyCascadedValidation() {
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		DemographicStatistics statistics = new DemographicStatistics();
		statistics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "inhabitantsPerAddress<K>[null, Lyon].street" );

		invalidAddress = new Address( "rue Garibaldi", new City( "L" ) );
		statistics = new DemographicStatistics();
		statistics.put( invalidAddress, 2 );

		constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "inhabitantsPerAddress<K>[rue Garibaldi, L].city.name" );
	}

	@Test
	public void testMapInvalidKeyClassLevelConstraint() {
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		DemographicStatistics statistics = new DemographicStatistics();
		statistics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "inhabitantsPerAddress<K>[rue Garibaldi, Lyon]" );
	}

	@Test
	public void testMapInvalidValueTypeArgument() {
		City city = new City( "Lyon" );
		State state = new State();
		state.put( city, null );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addressesPerCity[Lyon].<map value>" );
	}

	@Test
	public void testMapInvalidValueCascadedValidation() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addressesPerCity[Lyon].street" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "addressesPerCity" )
								.property( "street", true, city, null, Map.class, 1 )
						)
		);

		invalidAddress = new Address( "rue Garibaldi", new City( "L" ) );
		state = new State();
		state.put( city, invalidAddress );

		constraintViolations = validator.validate( state );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addressesPerCity[Lyon].city.name" );
	}

	@Test
	public void testMapInvalidValueClassLevelConstraint() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addressesPerCity[Lyon]" );
	}

	@Test
	public void testListTypeArgument() {
		Block block = new Block();
		block.add( null );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addresses[0].<list element>" );
	}

	@Test
	public void testListInvalidCascadedValidation() {
		Block block = new Block();
		block.add( new Address( null, new City( "Lyon" ) ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addresses[0].street" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "addresses" )
								.property( "street", true, null, 0, List.class, 0 )
						)
		);

		block = new Block();
		block.add( new Address( "rue Garibaldi", new City( "L" ) ) );

		constraintViolations = validator.validate( block );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addresses[0].city.name" );
	}

	@Test
	public void testListKeyClassLevelConstraint() {
		Block block = new Block();
		block.add( new Address( "rue Garibaldi", new City( "Lyon" ), "75003" ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "addresses[0]" );
	}

	@Test
	public void testNestedContainerElementConstraints() {
		Set<ConstraintViolation<MapOfLists>> constraintViolations = validator.validate( MapOfLists.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( MapOfLists.invalidKey() );
		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"map<K>[k].<map key>" );

		constraintViolations = validator.validate( MapOfLists.invalidList() );
		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"map[key1].<map value>" );

		constraintViolations = validator.validate( MapOfLists.invalidString() );
		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"map[key1].<map value>[0].<list element>",
				"map[key1].<map value>[1].<list element>" );

		constraintViolations = validator.validate( MapOfLists.reallyInvalid() );
		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"map<K>[k].<map key>",
				"map[k].<map value>",
				"map[k].<map value>[0].<list element>" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void testArrayPath() throws Exception {
		Set<ConstraintViolation<Region>> constraintViolations = validator.validate( new Region(
				Arrays.asList( new Address( null, null ) ),
				Arrays.asList( null )
		) );

		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"array[0].<iterable element>[0].street",
				"array[1].<iterable element>[0].<list element>"
		);
	}

	@Test
	public void testSetPath() throws Exception {
		Set<ConstraintViolation<AddressBook>> constraintViolations = validator.validate(
				new AddressBook()
						.add( new Address( null, null ) )
						.add( new Address( "Street", new City( "C" ) ) )
						.add( new Address( "Street2", new City( "C" ) ) )
						.add( new Address( "other", new City( "a" ) ) )
						.add( null )
		);

		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"addresses[].street",
				"addresses[].city.name",
				"addresses[].city.name",
				"addresses[].city.name",
				"addresses[].<iterable element>"
		);
	}

	private static class DemographicStatistics {

		private Map<@NotNull @Valid Address, @NotNull Integer> inhabitantsPerAddress = new HashMap<>();

		public void put(Address address, Integer count) {
			inhabitantsPerAddress.put( address, count );
		}
	}

	private static class Block {

		private List<@NotNull @Valid Address> addresses = new ArrayList<>();

		public void add(Address address) {
			addresses.add( address );
		}
	}

	private static class AddressBook {

		private final Set<@Valid @NotNull Address> addresses;

		private AddressBook() {
			this.addresses = new HashSet<>();
		}

		public AddressBook add(Address address) {
			this.addresses.add( address );
			return this;
		}
	}

	private static class Region {

		private final List<@Valid @NotNull Address>[] addresses;

		private Region(List<Address>... addresses) {
			this.addresses = addresses;
		}
	}

	private static class State {

		private Map<City, @NotNull @Valid Address> addressesPerCity = new HashMap<>();

		public void put(City city, Address address) {
			addressesPerCity.put( city, address );
		}
	}

	private static class MapOfLists {

		private Map<@Size(min = 2) String, @NotNull @Size(min = 2) List<Optional<@Size(min = 3) String>>> map;

		private static MapOfLists valid() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key", list );

			return foo;
		}

		private static MapOfLists invalidKey() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}

		private static MapOfLists invalidList() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "only one value" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static MapOfLists invalidString() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ), Optional.of( "2" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static MapOfLists reallyInvalid() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}
	}
}
