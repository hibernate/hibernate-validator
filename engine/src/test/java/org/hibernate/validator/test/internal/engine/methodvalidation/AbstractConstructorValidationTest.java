/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.engine.methodvalidation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.executable.ExecutableValidator;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl.ValidB2BRepository;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNodeKinds;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNodeNames;

/**
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class AbstractConstructorValidationTest {
	protected ExecutableValidator executableValidator;

	public abstract void setUp();

	public abstract String messagePrefix();

	@Test
	public void constructorParameterValidationYieldsConstraintViolation() throws Exception {
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				new String[] { null }
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator()
				.next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( messagePrefix() + "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		assertNodeKinds(
				constraintViolation.getPropertyPath(),
				ElementKind.CONSTRUCTOR,
				ElementKind.PARAMETER
		);
		assertNodeNames( constraintViolation.getPropertyPath(), "CustomerRepositoryImpl", "arg0" );
	}

	@Test
	public void cascadedConstructorParameterValidationYieldsConstraintViolation() throws Exception {
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( Customer.class ),
				new Customer[] { new Customer( null ) }
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( messagePrefix() + "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		Path path = constraintViolation.getPropertyPath();
		assertNodeKinds( path, ElementKind.CONSTRUCTOR, ElementKind.PARAMETER, ElementKind.PROPERTY );
		assertNodeNames( path, "CustomerRepositoryImpl", "arg0", "name" );
	}

	@Test
	public void constructorReturnValueValidationYieldsConstraintViolation() throws Exception {
		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor(),
				customerRepository
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( messagePrefix() + "{ValidB2BRepository.message}" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isSameAs( customerRepository );
		assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() )
				.isSameAs(
						ValidB2BRepository.class
				);

		assertNodeKinds(
				constraintViolation.getPropertyPath(),
				ElementKind.CONSTRUCTOR,
				ElementKind.RETURN_VALUE
		);
		assertNodeNames( constraintViolation.getPropertyPath(), "CustomerRepositoryImpl", "<return value>" );
	}

	@Test
	public void cascadedConstructorReturnValueValidationYieldsConstraintViolation() throws Exception {
		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				customerRepository
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( messagePrefix() + "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		Path path = constraintViolation.getPropertyPath();
		assertNodeKinds( path, ElementKind.CONSTRUCTOR, ElementKind.RETURN_VALUE, ElementKind.PROPERTY );
		assertNodeNames( path, "CustomerRepositoryImpl", "<return value>", "customer" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullParameterArrayThrowsException() throws Exception {
		executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( Customer.class ),
				null
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "null passed as group name.")
	public void testNullGroupsVarargThrowsException() throws Exception {
		executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				new String[] { "foo" },
				(Class<?>) null
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000116.*")
	public void testPassingNullAsConstructorReturnValueThrowsException() throws Exception {
		executableValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor(),
				null
		);
	}
}
