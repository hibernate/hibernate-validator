/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation.getter;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNull;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class ImplicitValidateOnExecutionDoesNotTriggerGetterValidationTest {
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Delivery.class )
				.addClass( DeliveryService.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	DeliveryService deliveryService;

	@Test
	public void testValidationOfConstrainedGetter() {
		Delivery delivery = deliveryService.getAnotherDelivery();
		assertNull(
				"the constraint is invalid, but no violation exception is expected since " +
						"@ValidateOnExecution(type=IMPLICIT) on the type-level should have no effect " +
						"and thus the default settings apply",
				delivery
		);
	}
}
