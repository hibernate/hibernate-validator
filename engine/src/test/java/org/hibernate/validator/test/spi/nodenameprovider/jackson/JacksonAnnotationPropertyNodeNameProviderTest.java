/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.spi.nodenameprovider.jackson;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;

import org.hibernate.validator.HibernateValidator;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.annotations.Test;

public class JacksonAnnotationPropertyNodeNameProviderTest {
	@Test
	public void mapTest() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new JacksonAnnotationPropertyNodeNameProvider() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		Car testInstance = new Car();
		testInstance.mapAsPropertyHolder.put( "engine_property", new Engine( 1000 ) );

		Set<ConstraintViolation<Car>> violations = validator.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals(
				violation.getPropertyPath().toString(), "map_as_property_holder[engine_property].horse_power.power" );
	}

	private static class Car {
		@Valid
		@JsonProperty(value = "map_as_property_holder")
		private Map<String, Object> mapAsPropertyHolder = new HashMap<>();
	}

	private static class Engine {
		@Valid
		private HorsePower horsePower;

		Engine(int horsePower) {
			this.horsePower = new HorsePower( horsePower );
		}

		@JsonProperty("horse_power")
		public HorsePower getHorsePower() {
			return horsePower;
		}
	}

	private static class HorsePower {
		@Max(500)
		private int power;

		HorsePower(int power) {
			this.power = power;
		}
	}
}
