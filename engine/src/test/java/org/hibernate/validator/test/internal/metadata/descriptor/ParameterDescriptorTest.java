/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.Scope;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerComplex;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.CustomerRepositoryExtComplex;

import static org.hibernate.validator.testutil.DescriptorAssert.assertThat;
import static org.hibernate.validator.testutil.ValidatorUtil.getParameterDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Gunnar Morling
 */
public class ParameterDescriptorTest {
	private ParameterDescriptor createCustomerParameter1;
	private ParameterDescriptor createCustomerParameter2;
	private ParameterDescriptor parameterWithConversions;

	@BeforeMethod
	public void setUpDescriptors() {
		createCustomerParameter1 = getParameterDescriptor(
				CustomerRepositoryExt.class,
				"createCustomer",
				new Class<?>[] { CharSequence.class, String.class },
				0
		);
		createCustomerParameter2 = getParameterDescriptor(
				CustomerRepositoryExt.class,
				"createCustomer",
				new Class<?>[] { CharSequence.class, String.class },
				1
		);
		parameterWithConversions = getParameterDescriptor(
				CustomerRepositoryExt.class,
				"modifyCustomer",
				new Class<?>[] { Customer.class },
				0
		);
	}

	@Test
	public void testGetElementClass() {
		assertEquals( createCustomerParameter1.getElementClass(), CharSequence.class );
		assertEquals( createCustomerParameter2.getElementClass(), String.class );
	}

	@Test
	public void testHasConstraints() {
		assertFalse( createCustomerParameter1.hasConstraints() );
		assertTrue( createCustomerParameter2.hasConstraints() );
	}

	@Test
	public void testGetConstraintDescriptors() {
		assertTrue( createCustomerParameter1.getConstraintDescriptors().isEmpty() );

		assertEquals( createCustomerParameter2.getConstraintDescriptors().size(), 1 );
		assertEquals(
				createCustomerParameter2.getConstraintDescriptors()
						.iterator()
						.next()
						.getAnnotation()
						.annotationType(),
				NotNull.class
		);
	}

	@Test(enabled = false, description = "Temporarily disabled due to HV-443")
	public void testFindConstraintLookingAtLocalElement() {
		Set<ConstraintDescriptor<?>> constraintDescriptors =
				createCustomerParameter2.findConstraints()
						.lookingAt( Scope.LOCAL_ELEMENT )
						.getConstraintDescriptors();

		assertEquals(
				constraintDescriptors.size(),
				0,
				"No local constraint for CustomerRepositoryExt#createCustomer(), arg1, expected."
		);

		ParameterDescriptor createCustomerParameter2OnBaseType = getParameterDescriptor(
				CustomerRepository.class,
				"createCustomer",
				new Class<?>[] { CharSequence.class, String.class },
				1
		);

		constraintDescriptors =
				createCustomerParameter2OnBaseType.findConstraints()
						.lookingAt( Scope.LOCAL_ELEMENT )
						.getConstraintDescriptors();

		assertEquals(
				constraintDescriptors.size(),
				1,
				"One local constraint for CustomerRepository#createCustomer(), arg1, expected."
		);
	}

	@Test
	public void testFindConstraintLookingAtHierarchy() {
		Set<ConstraintDescriptor<?>> constraintDescriptors =
				createCustomerParameter2.findConstraints()
						.lookingAt( Scope.HIERARCHY )
						.getConstraintDescriptors();

		assertEquals(
				constraintDescriptors.size(),
				1,
				"One hierarchy constraint for CustomerRepositoryExt#createCustomer(), arg1, expected."
		);
	}

	@Test
	public void testGetIndex() {
		assertEquals( createCustomerParameter1.getIndex(), 0 );
		assertEquals( createCustomerParameter2.getIndex(), 1 );
	}

	@Test
	public void testGetName() {
		assertEquals( createCustomerParameter1.getName(), "arg0" );
		assertEquals( createCustomerParameter2.getName(), "arg1" );
	}

	@Test
	public void testIsCascaded() {
		assertFalse( createCustomerParameter1.isCascaded() );

		ParameterDescriptor saveCustomerParameter = getParameterDescriptor(
				CustomerRepositoryExt.class, "saveCustomer", new Class<?>[] { Customer.class }, 0
		);
		assertTrue( saveCustomerParameter.isCascaded() );
	}

	@Test
	public void testGetGroupConversions() {
		Set<GroupConversionDescriptor> groupConversions = parameterWithConversions.getGroupConversions();

		assertThat( groupConversions ).hasSize( 2 );
		assertThat( groupConversions ).containsConversion( Default.class, CustomerBasic.class );
		assertThat( groupConversions ).containsConversion(
				CustomerRepositoryExtComplex.class,
				CustomerComplex.class
		);
	}
}
