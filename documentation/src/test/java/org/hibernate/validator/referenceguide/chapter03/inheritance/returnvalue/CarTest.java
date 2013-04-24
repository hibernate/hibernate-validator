package org.hibernate.validator.referenceguide.chapter03.inheritance.returnvalue;

import java.lang.reflect.Method;

import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;
import javax.validation.executable.ExecutableValidator;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CarTest {

	private static ExecutableValidator executableValidator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
	}

	@Test
	public void returnValueConstraintsAddUp() throws Exception {
		Car object = new Car();
		Method method = Car.class.getMethod( "getPassengers" );
		Object returnValue = Collections.<Person>emptyList();
		Set<ConstraintViolation<Car>> violations = executableValidator.validateReturnValue(
				object,
				method,
				returnValue
		);

		assertEquals( 1, violations.size() );
		assertEquals(
				Size.class,
				violations.iterator()
						.next()
						.getConstraintDescriptor()
						.getAnnotation()
						.annotationType()
		);
	}
}
