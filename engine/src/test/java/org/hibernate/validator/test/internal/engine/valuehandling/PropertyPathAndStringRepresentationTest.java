/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PropertyPathAndStringRepresentationTest {

	private Validator validator;

	@BeforeClass
	public void setupValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	public void testMapInvalidKeyTypeArgument() {
		DemographicStatistics statictics = new DemographicStatistics();
		statictics.put( null, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statictics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[].<map key>" ); // the key is null, thus the '[]'
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "inhabitantsPerAddress" )
						.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, null, null, "K" )
		);
	}

	@Test
	public void testMapInvalidKeyCascadedValidation() {
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		DemographicStatistics statictics = new DemographicStatistics();
		statictics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statictics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[null, Lyon].street" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "inhabitantsPerAddress" )
						.property( "street", true, invalidAddress, null, "K" )
		);

		invalidAddress = new Address( "rue Garibaldi", new City( "L" ) );
		statictics = new DemographicStatistics();
		statictics.put( invalidAddress, 2 );

		constraintViolations = validator.validate( statictics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[rue Garibaldi, L].city.name" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "inhabitantsPerAddress" )
						.property( "city", true, invalidAddress, null, "K" )
						.property( "name" )
		);
	}

	@Test
	public void testMapInvalidKeyClassLevelConstraint() {
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		DemographicStatistics statictics = new DemographicStatistics();
		statictics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statictics );

		assertCorrectPropertyPaths( constraintViolations, "inhabitantsPerAddress<K>[rue Garibaldi, Lyon]" );
		assertCorrectConstraintTypes( constraintViolations, ValidLyonZipCode.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "inhabitantsPerAddress" )
						.bean( true, invalidAddress, null, "K" )
		);
	}

	@Test
	public void testMapInvalidValueTypeArgument() {
		City city = new City( "Lyon" );
		State state = new State();
		state.put( city, null );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon].<map value>" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addressesPerCity" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, city, null, "V" )
		);
	}

	@Test
	public void testMapInvalidValueCascadedValidation() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon].street" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addressesPerCity" )
						.property( "street", true, city, null, "V" )
		);

		invalidAddress = new Address( "rue Garibaldi", new City( "L" ) );
		state = new State();
		state.put( city, invalidAddress );

		constraintViolations = validator.validate( state );

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon].city.name" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addressesPerCity" )
						.property( "city", true, city, null, "V" )
						.property( "name" )
		);
	}

	@Test
	public void testMapInvalidValueClassLevelConstraint() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertCorrectPropertyPaths( constraintViolations, "addressesPerCity[Lyon]" );
		assertCorrectConstraintTypes( constraintViolations, ValidLyonZipCode.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addressesPerCity" )
						.bean( true, city, null, "V" )
		);
	}

	@Test
	public void testListTypeArgument() {
		Block block = new Block();
		block.add( null );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPaths( constraintViolations, "addresses[0].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addresses" )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, "E" )
		);
	}

	@Test
	public void testListInvalidCascadedValidation() {
		Block block = new Block();
		block.add( new Address( null, new City( "Lyon" ) ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPaths( constraintViolations, "addresses[0].street" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addresses" )
						.property( "street", true, null, 0, "E" )
		);

		block = new Block();
		block.add( new Address( "rue Garibaldi", new City( "L" ) ) );

		constraintViolations = validator.validate( block );

		assertCorrectPropertyPaths( constraintViolations, "addresses[0].city.name" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addresses" )
						.property( "city", true, null, 0, "E" )
						.property( "name" )
		);
	}

	@Test
	public void testListKeyClassLevelConstraint() {
		Block block = new Block();
		block.add( new Address( "rue Garibaldi", new City( "Lyon" ), "75003" ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertCorrectPropertyPaths( constraintViolations, "addresses[0]" );
		assertCorrectConstraintTypes( constraintViolations, ValidLyonZipCode.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "addresses" )
						.bean( true, null, 0, "E" )
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

	private static class State {

		private Map<City, @NotNull @Valid Address> addressesPerCity = new HashMap<>();

		public void put(City city, Address address) {
			addressesPerCity.put( city, address );
		}
	}

	@ValidLyonZipCode
	private static class Address {
		@NotNull
		private String street;
		@Valid
		private City city;
		private String zipCode;

		public Address(String street, City city) {
			this.street = street;
			this.city = city;
		}

		public Address(String street, City city, String zipCode) {
			this.street = street;
			this.city = city;
			this.zipCode = zipCode;
		}

		@Override
		public String toString() {
			return street + ", " + city;
		}
	}

	private static class City {
		@Size(min = 3)
		private String name;

		public City(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	@Target({ TYPE, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { ValidLyonZipCodeValidator.class })
	@Documented
	public @interface ValidLyonZipCode {

		String message() default "{org.hibernate.validator.test.internal.engine.valuehandling.ValidLyonZipCode.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ValidLyonZipCodeValidator implements ConstraintValidator<ValidLyonZipCode, Address> {

		@Override
		public boolean isValid(Address address, ConstraintValidatorContext context) {
			if ( address == null || address.zipCode == null || address.city == null || !"Lyon".equals( address.city.name ) ) {
				return true;
			}

			return address.zipCode.length() == 5 && address.zipCode.startsWith( "6900" );
		}
	}

}
