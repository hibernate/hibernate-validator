/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import org.junit.jupiter.api.Test;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ElementDescriptorTest {

	@Test
	public void testGetTypeForConstrainedBean() {
		Validator validator = ValidatorUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Customer.class );
		assertEquals( Customer.class, beanDescriptor.getElementClass(), "Wrong type." );
	}

	@Test
	public void testGetTypeForConstrainedProperty() {
		ElementDescriptor elementDescriptor = ValidatorUtil.getPropertyDescriptor( Order.class, "orderNumber" );
		assertEquals( Integer.class, elementDescriptor.getElementClass(), "Wrong type." );
	}

	@Test
	public void testThatMethodLevelConstraintsAreNotReflectedByBeanDescriptor() {
		BeanDescriptor beanDescriptor = getValidator().getConstraintsForClass( CustomerRepository.class );

		Set<ConstraintDescriptor<?>> constraintDescriptors = beanDescriptor.getConstraintDescriptors();
		assertEquals( 1, constraintDescriptors.size(), "Only the class-level @ScriptAssert is expected." );

		constraintDescriptors = beanDescriptor.findConstraints()
				.declaredOn( ElementType.PARAMETER )
				.getConstraintDescriptors();
		assertEquals( 0, constraintDescriptors.size() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-95")
	public void testElementDescriptorForProperty() {
		ElementDescriptor elementDescriptor = ValidatorUtil.getPropertyDescriptor( Order.class, "orderNumber" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = elementDescriptor.getConstraintDescriptors();
		assertEquals( 1, constraintDescriptors.size(), "There should be a descriptor" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-95")
	public void testElementDescriptorImmutable() {
		ElementDescriptor elementDescriptor = ValidatorUtil.getPropertyDescriptor( Order.class, "orderNumber" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = elementDescriptor.getConstraintDescriptors();

		assertThatThrownBy( () -> constraintDescriptors.add( null ) )
				.isInstanceOf( UnsupportedOperationException.class );

		assertThatThrownBy( () -> constraintDescriptors.remove( constraintDescriptors.iterator().next() ) )
				.isInstanceOf( UnsupportedOperationException.class );
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
		assertEquals( "order", propertyDescriptor.getPropertyName() );
	}
}
