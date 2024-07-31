package org.hibernate.validator.referenceguide.chapter03.parameterscriptassert;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.constraints.ParameterScriptAssert;

import org.junit.BeforeClass;
import org.junit.Test;

public class ParameterScriptAssertTest {

	private static ExecutableValidator executableValidator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
	}

	@Test
	public void validateParameters() throws Exception {
		Car object = new Car();
		Method method = Car.class.getMethod( "load", List.class, List.class );
		Object[] parameterValues = {
				Arrays.asList( new Person() ),
				Arrays.asList( new PieceOfLuggage(), new PieceOfLuggage(), new PieceOfLuggage() )
		};

		Set<ConstraintViolation<Car>> violations = executableValidator.validateParameters(
				object,
				method,
				parameterValues
		);

		assertEquals( 1, violations.size() );
		Class<? extends Annotation> constraintType = violations.iterator()
				.next()
				.getConstraintDescriptor()
				.getAnnotation()
				.annotationType();
		assertEquals( ParameterScriptAssert.class, constraintType );
	}
}
