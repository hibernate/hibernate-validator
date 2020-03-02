package org.hibernate.validator.test.internal.engine.path.stringrepresentation;/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPathStringRepresentations;

import java.lang.reflect.Constructor;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.ParameterScriptAssert;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ConstructorPathStringRepresentationTest extends AbstractPathStringRepresentationTest {

	@Test
	public void testConstructorParameterPath() throws Exception {
		Constructor<City> constructor = City.class.getConstructor( String.class );
		Set<ConstraintViolation<City>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				new Object[] { "" }
		);
		assertCorrectPropertyPathStringRepresentations( constraintViolations, "City.name" );
	}

	@Test
	public void testConstructorMultipleParametersPath() throws Exception {
		Constructor<Address> constructor = Address.class.getConstructor( String.class, City.class );
		Set<ConstraintViolation<Address>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				new Object[] { null, new City( "" ) }
		);
		assertCorrectPropertyPathStringRepresentations( constraintViolations, "Address.street", "Address.city.name" );
	}

	@Test
	public void testConstructorReturnValuePath() throws Exception {
		Constructor<City> constructor = City.class.getConstructor( String.class );
		Set<ConstraintViolation<City>> constraintViolations = validator.forExecutables().validateConstructorReturnValue(
				constructor,
				new City( "" )
		);
		assertCorrectPropertyPathStringRepresentations( constraintViolations, "City.<return value>.name" );
	}

	@Test
	public void testAnotherConstructorReturnValuePath() throws Exception {
		Constructor<Address> constructor = Address.class.getConstructor( String.class, City.class, String.class );
		Set<ConstraintViolation<Address>> constraintViolations = validator.forExecutables().validateConstructorReturnValue(
				constructor,
				new Address( "str", new City( "Lyon" ), "invalid zip" )
		);
		assertCorrectPropertyPathStringRepresentations( constraintViolations, "Address.<return value>" );
	}

	@Test
	public void testConstructorCrossPath() throws Exception {
		Constructor<SmartCity> constructor = SmartCity.class.getConstructor( String.class, boolean.class );
		Set<ConstraintViolation<SmartCity>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				new Object[] { "good name", true }
		);

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "SmartCity.<cross-parameter>" );
	}

	private static class SmartCity extends City {

		@SuppressWarnings("unused")
		private final boolean isSmart;

		@ParameterScriptAssert(lang = "groovy", script = "false")
		public SmartCity(@Size(min = 3) String name, boolean isSmart) {
			super( name );
			this.isSmart = isSmart;
		}
	}
}
