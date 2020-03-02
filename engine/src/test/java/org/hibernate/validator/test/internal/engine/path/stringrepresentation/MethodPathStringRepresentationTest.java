/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.stringrepresentation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPathStringRepresentations;

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.ParameterScriptAssert;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class MethodPathStringRepresentationTest extends AbstractPathStringRepresentationTest {

	private CityService cityService;

	@BeforeMethod
	public void setUp() throws Exception {
		this.cityService = new CityServiceImpl();
	}

	@Test
	public void testMethodParameterPath() throws Exception {
		Method method = CityService.class.getDeclaredMethod( "findByName", String.class );
		Set<ConstraintViolation<CityService>> constraintViolations = validator.forExecutables()
				.validateParameters( cityService, method, new Object[] { null } );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "findByName.name" );
	}

	@Test
	public void testMethodMultipleParametersPath() throws Exception {
		Method method = CityService.class.getDeclaredMethod( "findByCityAndStreet", City.class, String.class );
		Set<ConstraintViolation<CityService>> constraintViolations = validator.forExecutables()
				.validateParameters( cityService, method, new Object[] { null, null } );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "findByCityAndStreet.city", "findByCityAndStreet.street" );
	}

	@Test
	public void testMethodReturnValuePath() throws Exception {
		Method method = CityService.class.getDeclaredMethod( "findByName", String.class );
		Set<ConstraintViolation<CityService>> constraintViolations = validator.forExecutables()
				.validateReturnValue( cityService, method, null );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "findByName.<return value>" );
	}

	@Test
	public void testMethodReturnValuePropertyPath() throws Exception {
		Method method = CityService.class.getDeclaredMethod( "findByCityAndStreet", City.class, String.class );
		Set<ConstraintViolation<CityService>> constraintViolations = validator.forExecutables()
				.validateReturnValue( cityService, method, new Address( null, new City( "" ) ) );

		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"findByCityAndStreet.<return value>.city.name",
				"findByCityAndStreet.<return value>.street"
		);
	}

	@Test
	public void testMethodCrossPath() throws Exception {
		Method method = CityService.class.getDeclaredMethod( "findByCityAndStreetAndZip", City.class, String.class, String.class );
		Set<ConstraintViolation<CityService>> constraintViolations = validator.forExecutables()
				.validateParameters( cityService, method, new Object[] { null, null, null } );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "findByCityAndStreetAndZip.<cross-parameter>" );
	}

	private interface CityService {

		@NotNull
		City findByName(@NotNull @Size(min = 3) String name);

		@Valid
		Address findByCityAndStreet(@NotNull City city, @NotNull String street);

		@ParameterScriptAssert(lang = "groovy", script = "false")
		Address findByCityAndStreetAndZip(City city, String street, String zip);
	}

	private static class CityServiceImpl implements CityService {

		@Override
		public City findByName(String name) {
			return new City( name );
		}

		@Override
		public Address findByCityAndStreet(City city, String street) {
			return new Address( street, city );
		}

		@Override
		public Address findByCityAndStreetAndZip(City city, String street, String zip) {
			return null;
		}
	}
}
