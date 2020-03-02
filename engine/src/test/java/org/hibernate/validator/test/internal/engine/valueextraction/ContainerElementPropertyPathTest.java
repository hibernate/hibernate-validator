/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.testutils.CandidateForTck;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@CandidateForTck
public class ContainerElementPropertyPathTest {

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "inhabitantsPerAddress" )
								.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, null, null, Map.class, 0 )
						)
		);
	}

	@Test
	public void testMapInvalidKeyCascadedValidation() {
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		DemographicStatistics statictics = new DemographicStatistics();
		statictics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statictics );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "inhabitantsPerAddress" )
								.property( "street", true, invalidAddress, null, Map.class, 0 )
						)
		);

		invalidAddress = new Address( "rue Garibaldi", new City( "L" ) );
		statictics = new DemographicStatistics();
		statictics.put( invalidAddress, 2 );

		constraintViolations = validator.validate( statictics );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "inhabitantsPerAddress" )
								.property( "city", true, invalidAddress, null, Map.class, 0 )
								.property( "name" )
						)
		);
	}

	@Test
	public void testMapInvalidKeyClassLevelConstraint() {
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		DemographicStatistics statictics = new DemographicStatistics();
		statictics.put( invalidAddress, 2 );

		Set<ConstraintViolation<DemographicStatistics>> constraintViolations = validator.validate( statictics );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ValidLyonZipCode.class )
						.withPropertyPath( pathWith()
								.property( "inhabitantsPerAddress" )
								.bean( true, invalidAddress, null, Map.class, 0 )
						)
		);
	}

	@Test
	public void testMapInvalidValueTypeArgument() {
		City city = new City( "Lyon" );
		State state = new State();
		state.put( city, null );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "addressesPerCity" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, city, null, Map.class, 1 )
						)
		);
	}

	@Test
	public void testMapInvalidValueCascadedValidation() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( null, new City( "Lyon" ) );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "addressesPerCity" )
								.property( "city", true, city, null, Map.class, 1 )
								.property( "name" )
						)
		);
	}

	@Test
	public void testMapInvalidValueClassLevelConstraint() {
		City city = new City( "Lyon" );
		Address invalidAddress = new Address( "rue Garibaldi", new City( "Lyon" ), "75003" );
		State state = new State();
		state.put( city, invalidAddress );

		Set<ConstraintViolation<State>> constraintViolations = validator.validate( state );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ValidLyonZipCode.class )
						.withPropertyPath( pathWith()
								.property( "addressesPerCity" )
								.bean( true, city, null, Map.class, 1 )
						)
		);
	}

	@Test
	public void testListTypeArgument() {
		Block block = new Block();
		block.add( null );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "addresses" )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						)
		);
	}

	@Test
	public void testListInvalidCascadedValidation() {
		Block block = new Block();
		block.add( new Address( null, new City( "Lyon" ) ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "addresses" )
								.property( "city", true, null, 0, List.class, 0 )
								.property( "name" )
						)
		);
	}

	@Test
	public void testListKeyClassLevelConstraint() {
		Block block = new Block();
		block.add( new Address( "rue Garibaldi", new City( "Lyon" ), "75003" ) );

		Set<ConstraintViolation<Block>> constraintViolations = validator.validate( block );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ValidLyonZipCode.class )
						.withPropertyPath( pathWith()
								.property( "addresses" )
								.bean( true, null, 0, List.class, 0 )
						)
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
