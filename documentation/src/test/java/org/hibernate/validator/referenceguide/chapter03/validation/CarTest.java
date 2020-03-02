package org.hibernate.validator.referenceguide.chapter03.validation;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path.MethodNode;
import jakarta.validation.Path.Node;
import jakarta.validation.Path.ParameterNode;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;

import org.junit.BeforeClass;
import org.junit.Test;

public class CarTest {

	private static ExecutableValidator executableValidator;

	@BeforeClass
	public static void setUpValidator() {
		//tag::setUpValidator[]
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
		//end::setUpValidator[]
	}

	@Test
	public void validateParameters() throws Exception {
		//tag::validateParameters[]
		Car object = new Car( "Morris" );
		Method method = Car.class.getMethod( "drive", int.class );
		Object[] parameterValues = { 80 };
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
		assertEquals( Max.class, constraintType );
		//end::validateParameters[]
	}

	@Test
	public void validateReturnValue() throws Exception {
		//tag::validateReturnValue[]
		Car object = new Car( "Morris" );
		Method method = Car.class.getMethod( "getPassengers" );
		Object returnValue = Collections.<Passenger>emptyList();
		Set<ConstraintViolation<Car>> violations = executableValidator.validateReturnValue(
				object,
				method,
				returnValue
		);

		assertEquals( 1, violations.size() );
		Class<? extends Annotation> constraintType = violations.iterator()
				.next()
				.getConstraintDescriptor()
				.getAnnotation()
				.annotationType();
		assertEquals( Size.class, constraintType );
		//end::validateReturnValue[]
	}

	@Test
	public void validateConstructorParameters() throws Exception {
		//tag::validateConstructorParameters[]
		Constructor<Car> constructor = Car.class.getConstructor( String.class );
		Object[] parameterValues = { null };
		Set<ConstraintViolation<Car>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);

		assertEquals( 1, violations.size() );
		Class<? extends Annotation> constraintType = violations.iterator()
				.next()
				.getConstraintDescriptor()
				.getAnnotation()
				.annotationType();
		assertEquals( NotNull.class, constraintType );
		//end::validateConstructorParameters[]
	}

	@Test
	public void validateConstructorReturnValue() throws Exception {
		//tag::validateConstructorReturnValue[]
		//constructor for creating racing cars
		Constructor<Car> constructor = Car.class.getConstructor( String.class, String.class );
		Car createdObject = new Car( "Morris", null );
		Set<ConstraintViolation<Car>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				createdObject
		);

		assertEquals( 1, violations.size() );
		Class<? extends Annotation> constraintType = violations.iterator()
				.next()
				.getConstraintDescriptor()
				.getAnnotation()
				.annotationType();
		assertEquals( ValidRacingCar.class, constraintType );
		//end::validateConstructorReturnValue[]
	}

	@Test
	public void retrieveMethodAndParameterInformation() throws Exception {
		//tag::retrieveMethodAndParameterInformation[]
		Car object = new Car( "Morris" );
		Method method = Car.class.getMethod( "drive", int.class );
		Object[] parameterValues = { 80 };
		Set<ConstraintViolation<Car>> violations = executableValidator.validateParameters(
				object,
				method,
				parameterValues
		);

		assertEquals( 1, violations.size() );
		Iterator<Node> propertyPath = violations.iterator()
				.next()
				.getPropertyPath()
				.iterator();

		MethodNode methodNode = propertyPath.next().as( MethodNode.class );
		assertEquals( "drive", methodNode.getName() );
		assertEquals( Arrays.<Class<?>>asList( int.class ), methodNode.getParameterTypes() );

		ParameterNode parameterNode = propertyPath.next().as( ParameterNode.class );
		assertEquals( "speedInMph", parameterNode.getName() );
		assertEquals( 0, parameterNode.getParameterIndex() );
		//end::retrieveMethodAndParameterInformation[]
	}
}
