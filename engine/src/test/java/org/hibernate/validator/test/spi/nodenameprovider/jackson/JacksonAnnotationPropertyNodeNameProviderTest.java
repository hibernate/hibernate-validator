/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.spi.nodenameprovider.jackson;

import static org.testng.Assert.assertEquals;

import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.testutil.TestForIssue;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Damir Alibegovic
 */
@TestForIssue(jiraKey = "HV-823")
public class JacksonAnnotationPropertyNodeNameProviderTest {
	private static final int VALID_HORSE_POWER = 150;
	private static final int INVALID_HORSE_POWER = 250;

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new JacksonAnnotationPropertyNodeNameProvider() )
				.buildValidatorFactory();

		validator = validatorFactory.getValidator();
	}

	@Test
	public void jsonPropertyOnFieldIsUsedForPathResolution() {
		Car testInstance = new Car( new Engine( INVALID_HORSE_POWER ) );

		Set<ConstraintViolation<Car>> violations = validator.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "engine.horse_power" );
	}

	@Test
	public void jsonPropertyOnGetterIsUsedForPathResolution() {
		int invalidNumberOfSeats = 0;
		Car testInstance = new Car( new Engine( VALID_HORSE_POWER ), invalidNumberOfSeats );

		Set<ConstraintViolation<Car>> violations = validator.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "number_of_seats" );
	}

	private class Car {
		@Valid
		private final Engine engine;

		@Min(1)
		private final int numberOfSeats;

		Car(Engine engine) {
			this( engine, 4 );
		}

		Car(Engine engine, int numberOfSeats) {
			this.engine = engine;
			this.numberOfSeats = numberOfSeats;
		}

		@JsonProperty("number_of_seats")
		public int getNumberOfSeats() {
			return numberOfSeats;
		}
	}

	private class Engine {
		@JsonProperty("horse_power")
		@Max(200)
		private final int horsePower;

		Engine(int horsePower) {
			this.horsePower = horsePower;
		}
	}
}
