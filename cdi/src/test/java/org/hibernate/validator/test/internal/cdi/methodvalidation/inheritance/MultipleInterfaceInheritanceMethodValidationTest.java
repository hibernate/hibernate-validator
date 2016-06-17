/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation.inheritance;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class MultipleInterfaceInheritanceMethodValidationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( ShipmentServiceFirstInHierarchy.class )
				.addClass( ShipmentServiceSecondInHierarchy.class )
				.addClass( Shipment.class )
				.addClass( ShipmentServiceImpl.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	ShipmentServiceSecondInHierarchy shipmentService;

	@Test
	public void testExecutableValidationUsesSettingFromHighestMethodInHierarchy() throws Exception {
		try {
			shipmentService.getShipment();
			fail( "Method invocation should have caused a ConstraintViolationException" );
		}
		catch (ConstraintViolationException e) {
			assertEquals(
					e.getConstraintViolations()
							.iterator()
							.next()
							.getConstraintDescriptor()
							.getAnnotation()
							.annotationType(), NotNull.class
			);
		}
	}
}
