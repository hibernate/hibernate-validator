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

import static junit.framework.Assert.assertEquals;
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
		catch ( ConstraintViolationException e ) {
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
