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
import javax.validation.MethodValidator;
import javax.validation.Path;
import javax.validation.metadata.ElementDescriptor.Kind;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl.ValidB2BRepository;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertDescriptorKinds;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertElementClasses;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNodeNames;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ConstructorValidationTest {

	@Test
	public void constructorParameterValidationYieldsConstraintViolation() throws Exception {
		MethodValidator methodValidator = getValidator().forMethods();

		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = methodValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				new String[] { null }
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		assertDescriptorKinds( constraintViolation.getPropertyPath(), Kind.CONSTRUCTOR, Kind.PARAMETER );
		assertNodeNames( constraintViolation.getPropertyPath(), "CustomerRepositoryImpl", "arg0" );
	}

	@Test
	public void cascadedConstructorParameterValidationYieldsConstraintViolation() throws Exception {
		MethodValidator methodValidator = getValidator().forMethods();

		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = methodValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( Customer.class ),
				new Customer[] { new Customer( null ) }
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		Path path = constraintViolation.getPropertyPath();
		assertDescriptorKinds( path, Kind.CONSTRUCTOR, Kind.PARAMETER, Kind.PROPERTY );
		assertNodeNames( path, "CustomerRepositoryImpl", "arg0", "name" );
		assertElementClasses( path, CustomerRepositoryImpl.class, Customer.class, String.class );
	}

	@Test
	public void constructorReturnValueValidationYieldsConstraintViolation() throws Exception {
		MethodValidator methodValidator = getValidator().forMethods();

		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = methodValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor(),
				customerRepository
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( "{ValidB2BRepository.message}" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isSameAs( customerRepository );
		assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() ).isSameAs(
				ValidB2BRepository.class
		);

		assertDescriptorKinds( constraintViolation.getPropertyPath(), Kind.CONSTRUCTOR, Kind.RETURN_VALUE );
		assertNodeNames( constraintViolation.getPropertyPath(), "CustomerRepositoryImpl", null );
	}

	@Test
	public void cascadedConstructorReturnValueValidationYieldsConstraintViolation() throws Exception {

		MethodValidator methodValidator = getValidator().forMethods();

		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = methodValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				customerRepository
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		Path path = constraintViolation.getPropertyPath();
		assertDescriptorKinds( path, Kind.CONSTRUCTOR, Kind.RETURN_VALUE, Kind.PROPERTY );
		assertNodeNames( path, "CustomerRepositoryImpl", null, "customer" );
		assertElementClasses( path, CustomerRepositoryImpl.class, CustomerRepositoryImpl.class, Customer.class );
	}
}
