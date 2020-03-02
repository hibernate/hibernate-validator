/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class MultipleInterfaceInheritanceMethodValidationTest extends Arquillian {

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
			assertThat(
					e.getConstraintViolations()
							.iterator()
							.next()
							.getConstraintDescriptor()
							.getAnnotation()
							.annotationType() )
					.isEqualTo( NotNull.class );
		}
	}
}
