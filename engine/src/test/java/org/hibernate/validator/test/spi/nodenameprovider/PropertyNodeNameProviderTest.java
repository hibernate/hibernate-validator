/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.spi.nodenameprovider;

import static org.testng.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Damir Alibegovic
 */
@TestForIssue(jiraKey = "HV-823")
public class PropertyNodeNameProviderTest {
	private static final String INVALID_BRAND_NAME = "BMW";
	private static final String VALID_BRAND_NAME = "Mercedes";

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new AnnotationPropertyNodeNameProvider( PropertyName.class ) )
				.buildValidatorFactory();

		validator = validatorFactory.getValidator();
	}

	@Test
	public void nameIsResolvedFromCustomAnnotationByUsingValidate() {
		Car testInstance = new Car( INVALID_BRAND_NAME );

		Set<ConstraintViolation<Car>> violations = validator.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand.brand_name" );
	}

	@Test
	public void nameIsResolvedFromCustomAnnotationByUsingValidateProperty() {
		Car testInstance = new Car( INVALID_BRAND_NAME );

		Set<ConstraintViolation<Car>> violations = validator.validateProperty( testInstance, "brand.name" );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand.brand_name" );
	}

	@Test
	public void nameIsResolvedFromCustomAnnotationByUsingValidateValue() {
		Set<ConstraintViolation<Brand>> violations = validator.validateValue( Brand.class, "name", INVALID_BRAND_NAME );
		ConstraintViolation<Brand> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand_name" );
	}

	@Test
	public void nameIsResolvedFromCustomAnnotationWithConstraintOnGetter() {
		int horsePower = 125;
		int speedInRpm = 500;
		Airplane testInstance = new Airplane( new Engine( horsePower, speedInRpm ) );

		Set<ConstraintViolation<Airplane>> violations = validator.validate( testInstance );
		ConstraintViolation<Airplane> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "turbojet_engine.speed_in_rpm" );
	}

	@Test
	public void propertyCanBeOfTypeMap() {
		int horsePower = 0;
		int speedInRpm = 1000;
		Car testInstance = new Car( VALID_BRAND_NAME );
		testInstance.addComponent( "engine", new Engine( horsePower, speedInRpm ) );

		Set<ConstraintViolation<Car>> violations = validator.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "components[engine].horse_power" );

	}

	@Test
	public void defaultValidatorFactoryUsesDefaultPropertyNodeNameProvider() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator val = factory.getValidator();

		Car testInstance = new Car( INVALID_BRAND_NAME );

		Set<ConstraintViolation<Car>> violations = val.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand.name" );
	}

	@Test
	public void defaultProviderUsesDefaultPropertyNodeNameProvider() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory();
		Validator val = validatorFactory.getValidator();

		Car testInstance = new Car( INVALID_BRAND_NAME );

		Set<ConstraintViolation<Car>> violations = val.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand.name" );
	}

	@Test
	public void hibernateValidatorUsesDefaultPropertyNodeProvider() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();
		Validator val = validatorFactory.getValidator();

		Car testInstance = new Car( INVALID_BRAND_NAME );

		Set<ConstraintViolation<Car>> violations = val.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand.name" );
	}

	@Test
	public void hibernateValidatorCanUseCustomPropertyNodeNameProvider() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new AnnotationPropertyNodeNameProvider( PropertyName.class ) )
				.buildValidatorFactory();
		Validator val = validatorFactory.getValidator();

		Car testInstance = new Car( INVALID_BRAND_NAME );

		Set<ConstraintViolation<Car>> violations = val.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand.brand_name" );
	}

	@Test
	public void hibernateValidatorFallsBackToDefaultPropertyNodeNameProvider() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( null )
				.buildValidatorFactory();
		Validator val = validatorFactory.getValidator();

		Car testInstance = new Car( INVALID_BRAND_NAME );

		Set<ConstraintViolation<Car>> violations = val.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "brand.name" );
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD })
	public @interface PropertyName {
		String value();
	}

	private class Car {
		@PropertyName("components")
		public final Map<String, @Valid Object> comps = new HashMap<>();

		@Valid
		public final Brand brand;

		Car(String brand) {
			this.brand = new Brand( brand );
		}

		public void addComponent(String name, Object component) {
			comps.put( name, component );
		}
	}

	private class Brand {
		@PropertyName("brand_name")
		@Size(min = 4)
		public final String name;

		Brand(String name) {
			this.name = name;
		}
	}

	private class Engine {
		@PropertyName("horse_power")
		@Min(1)
		public final int horsePower;

		@PropertyName("speed_in_rpm")
		public final int speedInRpm;

		public Engine(int horsePower, int speedInRpm) {
			this.horsePower = horsePower;
			this.speedInRpm = speedInRpm;
		}

		@Min(1000)
		public int getSpeedInRpm() {
			return speedInRpm;
		}
	}

	private class Airplane {
		@Valid
		@PropertyName("turbojet_engine")
		public final Engine engine;

		public Airplane(Engine engine) {
			this.engine = engine;
		}
	}
}
