/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.stringrepresentation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

public class ContainerElementPathStringRepresentationTest extends AbstractPathStringRepresentationTest {

	@Test
	public void testMapInvalidKeyTypeArgument() {
		DemographicStatistics statistics = new DemographicStatistics();
		statistics.put( null, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[].<map key>" ); // the key is null, thus the '[]'
	}

	@Test
	public void testMapInvalidKeyCascadedValidation() {
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		DemographicStatistics statistics = new DemographicStatistics();
		statistics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[null, Lyon].street" );

		invalidAddress = new Address( "rue Garibaldi", new City( "L" ) );
		statistics = new DemographicStatistics();
		statistics.put( invalidAddress, 2 );

		constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[rue Garibaldi, L].city.name" );
	}

	@Test
	public void testMapInvalidKeyClassLevelConstraint() {
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		DemographicStatistics statistics = new DemographicStatistics();
		statistics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statistics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[rue Garibaldi, Lyon]" );
	}

	@Test
	public void testMapInvalidValueTypeArgument() {
		City city = new City( "Lyon" );
		State state = new State();
		state.put( city, null );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon].<map value>" );
	}

	@Test
	public void testMapInvalidValueCascadedValidation() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon].street" );
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

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon].city.name" );
	}

	@Test
	public void testMapInvalidValueClassLevelConstraint() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon]" );
	}

	@Test
	public void testListTypeArgument() {
		Block block = new Block();
		block.add( null );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPaths( constraintViolations, "addresses[0].<list element>" );
	}

	@Test
	public void testListInvalidCascadedValidation() {
		Block block = new Block();
		block.add( new Address( null, new City( "Lyon" ) ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPaths( constraintViolations, "addresses[0].street" );
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

		assertCorrectPropertyPaths( constraintViolations, "addresses[0].city.name" );
	}

	@Test
	public void testListKeyClassLevelConstraint() {
		Block block = new Block();
		block.add( new Address( "rue Garibaldi", new City( "Lyon" ), "75003" ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPaths( constraintViolations, "addresses[0]" );
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

	private static class State {

		private Map<City, @NotNull @Valid Address> addressesPerCity = new HashMap<>();

		public void put(City city, Address address) {
			addressesPerCity.put( city, address );
		}
	}

}
