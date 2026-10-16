/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import static org.hibernate.validator.testutil.DescriptorAssert.assertThat;
import static org.hibernate.validator.testutils.ValidatorUtil.getPropertyDescriptor;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.GroupConversionDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerComplex;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerGetterBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerGetterComplex;
import org.hibernate.validator.test.internal.metadata.Order.OrderBasic;
import org.hibernate.validator.test.internal.metadata.Order.OrderComplex;

import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class PropertyDescriptorTest {

	@Test
	public void testGetGroupConversionsDeclaredOnField() {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(
				Customer.class,
				"orderList"
		);

		Set<GroupConversionDescriptor> groupConversions = propertyDescriptor.getGroupConversions();

		assertThat( groupConversions ).hasSize( 2 );
		assertThat( groupConversions ).containsConversion( CustomerBasic.class, OrderBasic.class );
		assertThat( groupConversions ).containsConversion(
				CustomerComplex.class,
				OrderComplex.class
		);
	}

	@Test
	public void testGetGroupConversionsDeclaredOnGetter() {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(
				Customer.class,
				"lastOrder"
		);

		Set<GroupConversionDescriptor> groupConversions = propertyDescriptor.getGroupConversions();

		assertThat( groupConversions ).hasSize( 2 );
		assertThat( groupConversions ).containsConversion(
				CustomerGetterBasic.class,
				OrderBasic.class
		);
		assertThat( groupConversions ).containsConversion(
				CustomerGetterComplex.class,
				OrderComplex.class
		);
	}

	@Test
	public void testConstraintDescriptorsOfOverriddenGetterHaveStableOrder() {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor( ChildWithSize.class, "name" );

		List<Integer> maxValues = propertyDescriptor.getConstraintDescriptors().stream()
				.map( ConstraintDescriptor::getAnnotation )
				.map( annotation -> ( (Size) annotation ).max() )
				.collect( Collectors.toList() );

		// constraints are exposed in the order they are collected from the class hierarchy,
		// the iteration order must not change between JVM runs
		assertEquals( maxValues, List.of( 20, 50 ) );
	}

	private static class ParentWithSize {
		@Size(max = 50)
		public String getName() {
			return null;
		}
	}

	private static class ChildWithSize extends ParentWithSize {
		@Size(max = 20)
		@Override
		public String getName() {
			return null;
		}
	}
}
