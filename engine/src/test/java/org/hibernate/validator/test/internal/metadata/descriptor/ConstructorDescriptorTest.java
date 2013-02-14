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
package org.hibernate.validator.test.internal.metadata.descriptor;

import java.util.List;
import java.util.Set;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.ValidB2BRepository;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.testutil.ValidatorUtil.getConstructorDescriptor;

/**
 * @author Gunnar Morling
 */
public class ConstructorDescriptorTest {

	@Test
	public void testGetElementClass() {
		ConstructorDescriptor constructorDescriptor = getConstructorDescriptor(
				CustomerRepositoryExt.class,
				String.class
		);

		assertThat( constructorDescriptor.getElementClass() ).isEqualTo( CustomerRepositoryExt.class );
	}

	@Test
	public void testGetParameterDescriptors() {
		ConstructorDescriptor constructorDescriptor = getConstructorDescriptor(
				CustomerRepositoryExt.class,
				String.class,
				Customer.class
		);

		List<ParameterDescriptor> parameterDescriptors = constructorDescriptor.getParameterDescriptors();
		assertThat( parameterDescriptors ).hasSize( 2 );

		ParameterDescriptor parameterDescriptor1 = parameterDescriptors.get( 0 );
		assertThat( parameterDescriptor1.getElementClass() ).isEqualTo( String.class );
		assertThat( parameterDescriptor1.getIndex() ).isEqualTo( 0 );
		assertThat( parameterDescriptor1.getName() ).isEqualTo( "arg0" );
		assertThat( parameterDescriptor1.hasConstraints() ).isTrue();
		assertThat( parameterDescriptor1.isCascaded() ).isFalse();

		ParameterDescriptor parameterDescriptor2 = parameterDescriptors.get( 1 );
		assertThat( parameterDescriptor2.getElementClass() ).isEqualTo( Customer.class );
		assertThat( parameterDescriptor2.getIndex() ).isEqualTo( 1 );
		assertThat( parameterDescriptor2.getName() ).isEqualTo( "arg1" );
		assertThat( parameterDescriptor2.hasConstraints() ).isFalse();
		assertThat( parameterDescriptor2.isCascaded() ).isTrue();
	}

	@Test
	public void testGetReturnValueDescriptor() {
		ConstructorDescriptor constructorDescriptor = getConstructorDescriptor(
				CustomerRepositoryExt.class,
				String.class
		);

		ReturnValueDescriptor returnValueDescriptor = constructorDescriptor.getReturnValueDescriptor();
		assertThat( returnValueDescriptor ).isNotNull();
		assertThat( returnValueDescriptor.getElementClass() ).isEqualTo( CustomerRepositoryExt.class );
		assertThat( returnValueDescriptor.hasConstraints() ).isTrue();
		assertThat( returnValueDescriptor.isCascaded() ).isTrue();

		Set<ConstraintDescriptor<?>> constraintDescriptors = returnValueDescriptor.getConstraintDescriptors();
		assertThat( constraintDescriptors ).hasSize( 1 );
		ConstraintDescriptor<?> descriptor = constraintDescriptors.iterator().next();
		assertThat( descriptor.getAnnotation().annotationType() ).isEqualTo( ValidB2BRepository.class );
	}
}
