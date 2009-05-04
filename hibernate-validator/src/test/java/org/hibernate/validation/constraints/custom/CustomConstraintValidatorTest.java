package org.hibernate.validation.constraints.custom;

import java.util.Set;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import javax.validation.PropertyDescriptor;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author Emmanuel Bernard
 */

public class CustomConstraintValidatorTest {

	@Test
	public void testInheritedConstraintValidationImpl() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Phone p = new Phone();
		p.size = -2;
		final PropertyDescriptor propertyDescriptor = validator.getConstraintsForClass( Phone.class )
				.getConstraintsForProperty( "size" );
		assertNotNull( propertyDescriptor );
		final Set<ConstraintViolation<Phone>> constraintViolations = validator.validate( p );
		assertEquals( 1, constraintViolations.size() );
	}

	public static class Phone {
		@Positive public int size;
	}
}
