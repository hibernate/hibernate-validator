package org.hibernate.validator.test.internal.engine;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.hibernate.validator.internal.AnnotationPropertyNodeNameProvider;

import org.junit.Test;

public class AnnotationPropertyNodeNameProviderTest {
	@Test
	public void nameIsResolvedFromFieldAnnotation() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new AnnotationPropertyNodeNameProvider( AlternativePropertyName.class ) )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		Car testInstance = new Car( "BMW" );

		Set<ConstraintViolation<Car>> violations = validator.validateProperty( testInstance, "model" );
		ConstraintViolation<Car> violation = violations.iterator().next();
		HibernateConstraintViolation<Car> hibernateViolation = violation.unwrap( HibernateConstraintViolation.class );

		assertEquals( 1, hibernateViolation.getResolvedPropertyPath().size() );
		assertEquals( "car_model_field", hibernateViolation.getResolvedPropertyPath().get( 0 ) );
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface AlternativePropertyName {
		String value();
	}

	private static class Car {
		@Max(2)
		@AlternativePropertyName(value = "car_model_field")
		public String model;

		Car(String model) {
			this.model = model;
		}
	}
}
