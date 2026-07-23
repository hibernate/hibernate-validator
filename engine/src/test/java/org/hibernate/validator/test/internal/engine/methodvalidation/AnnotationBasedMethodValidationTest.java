/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Hardy Ferentschik
 */
public class AnnotationBasedMethodValidationTest extends AbstractMethodValidationTest {

	@Override
	@BeforeEach
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

		assertThatThrownBy( () -> customerRepositoryValidatingProxy.iterableParameterWithCascadingTypeParameter( customers ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
					assertEquals( 1, e.getConstraintViolations().size() );

					ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "iterableParameterWithCascadingTypeParameter", List.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals(
							"iterableParameterWithCascadingTypeParameter.customer[1].name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( customer, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertArrayEquals( new Object[] { customers }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}
}
