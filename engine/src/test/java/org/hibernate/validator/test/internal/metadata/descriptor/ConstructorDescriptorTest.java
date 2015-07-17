/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.ValidB2BRepository;
import org.testng.annotations.Test;

import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;
import java.util.List;
import java.util.Set;

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
