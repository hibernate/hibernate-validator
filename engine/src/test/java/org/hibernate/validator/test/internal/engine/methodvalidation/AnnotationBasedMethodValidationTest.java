/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class AnnotationBasedMethodValidationTest extends AbstractMethodValidationTest {

	@Override
	@BeforeMethod
	protected void setUp() {
		validator = ValidatorUtil.getValidator();
		createProxy();
	}

	@Override
	protected String messagePrefix() {
		return "";
	}

	// TODO Move up once XML support is there for type level cascades
	@Test
	public void iterableParameterWithCascadingTypeParameter() {
		Customer customer = new Customer( null );
		List<Customer> customers = Arrays.asList( null, customer );

		try {
			customerRepositoryValidatingProxy.iterableParameterWithCascadingTypeParameter( customers );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "iterableParameterWithCascadingTypeParameter", List.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"iterableParameterWithCascadingTypeParameter.customer[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { customers } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}
}
