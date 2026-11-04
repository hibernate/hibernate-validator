/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import static org.hibernate.validator.testutil.DescriptorAssert.assertThat;
import static org.hibernate.validator.testutils.ValidatorUtil.getParameterDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.GroupConversionDescriptor;
import jakarta.validation.metadata.ParameterDescriptor;
import jakarta.validation.metadata.Scope;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerComplex;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.CustomerRepositoryExtComplex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Gunnar Morling
 */
public class ParameterDescriptorTest {
	private ParameterDescriptor createCustomerParameter1;
	private ParameterDescriptor createCustomerParameter2;
	private ParameterDescriptor parameterWithConversions;

	@BeforeEach
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
		assertEquals( CharSequence.class, createCustomerParameter1.getElementClass() );
		assertEquals( String.class, createCustomerParameter2.getElementClass() );
	}

	@Test
	public void testHasConstraints() {
		assertFalse( createCustomerParameter1.hasConstraints() );
		assertTrue( createCustomerParameter2.hasConstraints() );
	}

	@Test
	public void testGetConstraintDescriptors() {
		assertTrue( createCustomerParameter1.getConstraintDescriptors().isEmpty() );

		assertEquals( 1, createCustomerParameter2.getConstraintDescriptors().size() );
		assertEquals(
				NotNull.class,
				createCustomerParameter2.getConstraintDescriptors()
						.iterator()
						.next()
						.getAnnotation()
						.annotationType() );
	}

	@Disabled("Temporarily disabled due to HV-443")
	@Test
	public void testFindConstraintLookingAtLocalElement() {
		Set<ConstraintDescriptor<?>> constraintDescriptors =
				createCustomerParameter2.findConstraints()
						.lookingAt( Scope.LOCAL_ELEMENT )
						.getConstraintDescriptors();

		assertEquals(
				0,
				constraintDescriptors.size(),
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
				1,
				constraintDescriptors.size(),
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
				1,
				constraintDescriptors.size(),
				"One hierarchy constraint for CustomerRepositoryExt#createCustomer(), arg1, expected."
		);
	}

	@Test
	public void testGetIndex() {
		assertEquals( 0, createCustomerParameter1.getIndex() );
		assertEquals( 1, createCustomerParameter2.getIndex() );
	}

	@Test
	public void testGetName() {
		assertEquals( "firstName", createCustomerParameter1.getName() );
		assertEquals( "lastName", createCustomerParameter2.getName() );
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
