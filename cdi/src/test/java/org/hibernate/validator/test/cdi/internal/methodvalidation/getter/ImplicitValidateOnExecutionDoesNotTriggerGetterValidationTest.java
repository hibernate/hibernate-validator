/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class ImplicitValidateOnExecutionDoesNotTriggerGetterValidationTest extends Arquillian {
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Delivery.class )
				.addClass( DeliveryService.class )
				.addAsManifestResource( "beans.xml" );
	}

	@Inject
	DeliveryService deliveryService;

	@Test
	public void testValidationOfConstrainedGetter() {
		Delivery delivery = deliveryService.getAnotherDelivery();
		assertThat( delivery )
				.as( "the constraint is invalid, but no violation exception is expected since " +
						"@ValidateOnExecution(type=IMPLICIT) on the type-level should have no effect " +
						"and thus the default settings apply" )
				.isNull();
	}
}
