/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.annotation.ElementType;
import java.util.Set;

import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ElementDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.test.internal.metadata.ChildWithAtValid;
import org.hibernate.validator.test.internal.metadata.ChildWithoutAtValid;
import org.hibernate.validator.test.internal.metadata.ChildWithoutAtValid2;
import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.Order;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ElementDescriptorTest {

	@Test
	public void testGetTypeForConstrainedBean() {
		Validator validator = ValidatorUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Customer.class );
		assertEquals( beanDescriptor.getElementClass(), Customer.class, "Wrong type." );
	}

	@Test
	public void testGetTypeForConstrainedProperty() {
		ElementDescriptor elementDescriptor = ValidatorUtil.getPropertyDescriptor( Order.class, "orderNumber" );
		assertEquals( elementDescriptor.getElementClass(), Integer.class, "Wrong type." );
	}

	@Test
	public void testThatMethodLevelConstraintsAreNotReflectedByBeanDescriptor() {
		BeanDescriptor beanDescriptor = getValidator().getConstraintsForClass( CustomerRepository.class );

		Set<ConstraintDescriptor<?>> constraintDescriptors = beanDescriptor.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 1, "Only the class-level @ScriptAssert is expected." );

		constraintDescriptors = beanDescriptor.findConstraints()
				.declaredOn( ElementType.PARAMETER )
				.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-95")
	public void testElementDescriptorForProperty() {
		ElementDescriptor elementDescriptor = ValidatorUtil.getPropertyDescriptor( Order.class, "orderNumber" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = elementDescriptor.getConstraintDescriptors();
		assertTrue( constraintDescriptors.size() == 1, "There should be a descriptor" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-95")
	public void testElementDescriptorImmutable() {
		ElementDescriptor elementDescriptor = ValidatorUtil.getPropertyDescriptor( Order.class, "orderNumber" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = elementDescriptor.getConstraintDescriptors();

		try {
			constraintDescriptors.add( null );
			fail( "Set should be immutable" );
		}
		catch (UnsupportedOperationException e) {
			// success
		}

		try {
			constraintDescriptors.remove( constraintDescriptors.iterator().next() );
			fail( "Set should be immutable" );
		}
		catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testAtValidDefinedInHierarchyForPropertyDescriptor() {
		PropertyDescriptor propertyDescriptor = ValidatorUtil.getPropertyDescriptor(
				ChildWithoutAtValid.class,
				"order"
		);
		assertTrue(
				propertyDescriptor.isCascaded(),
				"@Valid defined on getter in super type should be reflected by PropertyDescriptor."
		);
	}

	@Test
	public void testAtValidDefinedLocallyForPropertyDescriptor() {
		PropertyDescriptor propertyDescriptor = ValidatorUtil.getPropertyDescriptor( ChildWithAtValid.class, "order" );
		assertTrue(
				propertyDescriptor.isCascaded(),
				"@Valid defined on local getter in type hierarchy should be reflected by PropertyDescriptor."
		);
	}

	@Test
	public void testAtValidNotDefinedForPropertyDescriptor() {
		PropertyDescriptor propertyDescriptor = ValidatorUtil.getPropertyDescriptor(
				ChildWithoutAtValid2.class,
				"order"
		);
		assertFalse(
				propertyDescriptor.isCascaded(),
				"@Valid given neither locally nor in hierarchy should be reflected by PropertyDescriptor."
		);
	}

	@Test
	public void testGetNameFromPropertyDescriptor() {
		PropertyDescriptor propertyDescriptor = ValidatorUtil.getPropertyDescriptor(
				ChildWithoutAtValid2.class,
				"order"
		);
		assertEquals( propertyDescriptor.getPropertyName(), "order" );
	}
}
