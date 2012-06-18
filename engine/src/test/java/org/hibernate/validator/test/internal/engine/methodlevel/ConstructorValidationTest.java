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
package org.hibernate.validator.test.internal.engine.methodlevel;

import java.util.Iterator;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path.Node;
import javax.validation.Validator;
import javax.validation.metadata.ElementDescriptor.Kind;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.methodlevel.model.Customer;
import org.hibernate.validator.test.internal.engine.methodlevel.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodlevel.service.CustomerRepositoryImpl.ValidB2BRepository;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * @author Gunnar Morling
 */
public class ConstructorValidationTest {

	@Test
	public void constructorParameterValidationYieldsConstraintViolation() throws Exception {

		Validator validator = getValidator();

		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = validator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				new String[] { null }
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		Iterator<Node> pathIterator = constraintViolation.getPropertyPath().iterator();

		Node constructorNode = pathIterator.next();
		assertThat( constructorNode.getElementDescriptor().getKind() ).isEqualTo( Kind.CONSTRUCTOR );
		//TODO HV-571: The name looks strange
		//assertThat(constructorNode.getName()).isEqualTo("");

		Node parameterNode = pathIterator.next();
		assertThat( parameterNode.getElementDescriptor().getKind() ).isEqualTo( Kind.PARAMETER );
		assertThat( parameterNode.getName() ).isEqualTo( "arg0" );

		assertThat( pathIterator.hasNext() ).isFalse();

		//TODO HV-571: Add more assertions
	}

	@Test
	public void cascadedConstructorParameterValidationYieldsConstraintViolation() throws Exception {

		Validator validator = getValidator();

		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = validator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( Customer.class ),
				new Customer[] { new Customer( null ) }
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		Iterator<Node> pathIterator = constraintViolation.getPropertyPath().iterator();

		Node constructorNode = pathIterator.next();
		assertThat( constructorNode.getElementDescriptor().getKind() ).isEqualTo( Kind.CONSTRUCTOR );
		assertThat(
				constructorNode.getElementDescriptor()
						.getElementClass()
		).isEqualTo( CustomerRepositoryImpl.class );

		//TODO HV-571: The name looks strange
		//assertThat(constructorNode.getName()).isEqualTo("");

		Node parameterNode = pathIterator.next();
		assertThat( parameterNode.getElementDescriptor().getKind() ).isEqualTo( Kind.PARAMETER );
		assertThat( parameterNode.getElementDescriptor().getElementClass() ).isEqualTo( Customer.class );
		assertThat( parameterNode.getName() ).isEqualTo( "arg0" );

		Node nameNode = pathIterator.next();
		assertThat( nameNode.getElementDescriptor().getKind() ).isEqualTo( Kind.PROPERTY );
		assertThat( nameNode.getElementDescriptor().getElementClass() ).isEqualTo( String.class );
		assertThat( nameNode.getName() ).isEqualTo( "name" );

		assertThat( pathIterator.hasNext() ).isFalse();

		//TODO HV-571: Add more assertions
	}

	@Test
	public void constructorReturnValueValidationYieldsConstraintViolation() throws Exception {

		Validator validator = getValidator();

		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = validator.validateConstructorReturnValue(
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

		Iterator<Node> pathIterator = constraintViolation.getPropertyPath().iterator();

		Node constructorNode = pathIterator.next();
		assertThat( constructorNode.getElementDescriptor().getKind() ).isEqualTo( Kind.CONSTRUCTOR );
		//TODO HV-571: The name looks strange
		//assertThat(constructorNode.getName()).isEqualTo("");

		Node parameterNode = pathIterator.next();
		assertThat( parameterNode.getElementDescriptor().getKind() ).isEqualTo( Kind.RETURN_VALUE );
		assertThat( parameterNode.getName() ).isEqualTo( "$retval" );

		assertThat( pathIterator.hasNext() ).isFalse();

		//TODO HV-571: Add more assertions
	}

	@Test
	public void cascadedConstructorReturnValueValidationYieldsConstraintViolation() throws Exception {

		Validator validator = getValidator();

		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = validator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				customerRepository
		);

		assertThat( violations ).hasSize( 1 );

		ConstraintViolation<CustomerRepositoryImpl> constraintViolation = violations.iterator().next();
		assertThat( constraintViolation.getMessage() ).isEqualTo( "may not be null" );
		assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( constraintViolation.getInvalidValue() ).isNull();

		Iterator<Node> pathIterator = constraintViolation.getPropertyPath().iterator();

		Node constructorNode = pathIterator.next();
		assertThat( constructorNode.getElementDescriptor().getKind() ).isEqualTo( Kind.CONSTRUCTOR );
		assertThat(
				constructorNode.getElementDescriptor()
						.getElementClass()
		).isEqualTo( CustomerRepositoryImpl.class );

		//TODO HV-571: The name looks strange
		//assertThat(constructorNode.getName()).isEqualTo("");

		Node parameterNode = pathIterator.next();
		assertThat( parameterNode.getElementDescriptor().getKind() ).isEqualTo( Kind.RETURN_VALUE );
		assertThat( parameterNode.getElementDescriptor().getElementClass() ).isEqualTo( CustomerRepositoryImpl.class );
		assertThat( parameterNode.getName() ).isEqualTo( "$retval" );

		Node nameNode = pathIterator.next();
		assertThat( nameNode.getElementDescriptor().getKind() ).isEqualTo( Kind.PROPERTY );
		assertThat( nameNode.getElementDescriptor().getElementClass() ).isEqualTo( Customer.class );
		assertThat( nameNode.getName() ).isEqualTo( "customer" );

		assertThat( pathIterator.hasNext() ).isFalse();

		//TODO HV-571: Add more assertions
	}
}
