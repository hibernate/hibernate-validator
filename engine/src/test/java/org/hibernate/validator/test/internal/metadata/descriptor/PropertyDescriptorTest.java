/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerComplex;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerGetterBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerGetterComplex;
import org.hibernate.validator.test.internal.metadata.Order.OrderBasic;
import org.hibernate.validator.test.internal.metadata.Order.OrderComplex;

import static org.hibernate.validator.testutil.DescriptorAssert.assertThat;
import static org.hibernate.validator.testutil.ValidatorUtil.getPropertyDescriptor;

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
}
