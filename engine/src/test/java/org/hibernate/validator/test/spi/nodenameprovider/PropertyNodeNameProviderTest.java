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
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.Length;

import org.testng.annotations.Test;

public class PropertyNodeNameProviderTest {
	@Test
	public void nameIsResolvedFromFieldAnnotation() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new AnnotationPropertyNodeNameProvider( AlternativePropertyName.class ) )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		Car testInstance = new Car( "BMW", 100 );

		Set<ConstraintViolation<Car>> violations = validator.validateProperty( testInstance, "model.name" );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "model.car_model_field" );
	}

	@Test
	public void validate() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new AnnotationPropertyNodeNameProvider( AlternativePropertyName.class ) )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		Car testInstance = new Car( "BMW", 100 );

		Set<ConstraintViolation<Car>> violations = validator.validate( testInstance );
		ConstraintViolation<Car> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "model.car_model_field" );
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface AlternativePropertyName {
		String value();
	}

	private static class Car {
		@Valid
		public Model model;

		@Valid
		public Engine engine;

		Car(String model, int horsePower) {
			this.model = new Model( model );
			this.engine = new Engine( horsePower );
		}
	}

	private static class Model {
		@Length(max = 2)
		@AlternativePropertyName(value = "car_model_field")
		public String name;

		Model(String model) {
			this.name = model;
		}
	}

	private static class Engine {
		@Valid
		private HorsePower horsePower;

		Engine(int horsePower) {
			this.horsePower = new HorsePower( horsePower );
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
