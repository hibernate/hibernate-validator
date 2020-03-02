package org.hibernate.validator.referenceguide.chapter08;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import jakarta.validation.Configuration;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.CrossParameterDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.ParameterDescriptor;
import jakarta.validation.metadata.ReturnValueDescriptor;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class XMLConfigurationTest {
	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		configuration.addMapping( XMLConfigurationTest.class.getResourceAsStream( "rental-station-mapping.xml" ) );
		ValidatorFactory factory = configuration.buildValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testXMLConstraintsApplied() {
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( RentalStation.class );

		assertTrue( beanDescriptor.isBeanConstrained() );

		ConstructorDescriptor constructorDescriptor = beanDescriptor.getConstraintsForConstructor();
		ReturnValueDescriptor returnValueDescriptor = constructorDescriptor.getReturnValueDescriptor();
		Set<ConstraintDescriptor<?>> constraintDescriptors = returnValueDescriptor.getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, ValidRentalStation.class );

		constructorDescriptor = beanDescriptor.getConstraintsForConstructor( String.class );
		List<ParameterDescriptor> parameterDescriptors = constructorDescriptor.getParameterDescriptors();
		constraintDescriptors = parameterDescriptors.get( 0 ).getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, NotNull.class );

		MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod( "getCustomers" );
		returnValueDescriptor = methodDescriptor.getReturnValueDescriptor();
		constraintDescriptors = returnValueDescriptor.getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, NotNull.class, Size.class );

		methodDescriptor = beanDescriptor.getConstraintsForMethod( "rentCar", Customer.class, Date.class, int.class );
		parameterDescriptors = methodDescriptor.getParameterDescriptors();

		constraintDescriptors = parameterDescriptors.get( 0 ).getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, NotNull.class );

		constraintDescriptors = parameterDescriptors.get( 1 ).getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, NotNull.class, Future.class );

		constraintDescriptors = parameterDescriptors.get( 2 ).getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, Min.class );
	}

	@Test
	public void testXMLCrossParameterConstraints() {
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Garage.class );

		MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod( "buildCar", java.util.List.class );
		CrossParameterDescriptor crossParameterDescriptor = methodDescriptor.getCrossParameterDescriptor();
		Set<ConstraintDescriptor<?>> constraintDescriptors = crossParameterDescriptor.getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, ELAssert.class );

		methodDescriptor = beanDescriptor.getConstraintsForMethod( "paintCar", int.class );
		ReturnValueDescriptor returnValueDescriptor = methodDescriptor.getReturnValueDescriptor();
		constraintDescriptors = returnValueDescriptor.getConstraintDescriptors();
		assertCorrectConstraintTypes( constraintDescriptors, ELAssert.class );
	}

	private void assertCorrectConstraintTypes( Set<ConstraintDescriptor<?>> constraintDescriptors,
											  Class<?>... constraints) {
		List<Class<?>> constraintAnnotations = new ArrayList<Class<?>>();
		constraintAnnotations.addAll( Arrays.asList( constraints ) );
		for ( ConstraintDescriptor descriptor : constraintDescriptors ) {
			Class<?> annotationType = descriptor.getAnnotation().annotationType();
			if ( constraintAnnotations.contains( annotationType ) ) {
				constraintAnnotations.remove( annotationType );
			}
			else {
				fail( "found constraint type " + annotationType + " which was not expected" );
			}
		}

		if ( !constraintAnnotations.isEmpty() ) {
			fail( "not all expected constraint types were found" );
		}
	}
}
