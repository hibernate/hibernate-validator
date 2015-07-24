/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import org.hibernate.validator.test.internal.metadata.ChildWithAtValid;
import org.hibernate.validator.test.internal.metadata.ChildWithoutAtValid;
import org.hibernate.validator.test.internal.metadata.ChildWithoutAtValid2;
import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.Order;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.util.Set;

import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;


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
		catch ( UnsupportedOperationException e ) {
			// success
		}

		try {
			constraintDescriptors.remove( null );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {
			// success
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
