/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import jakarta.inject.Inject;

import org.hibernate.validator.test.util.TestHelper;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class DisableExecutableValidationInXmlTest extends Arquillian {
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Repeater.class )
				.addClass( DefaultRepeater.class )
				.addAsResource(
						TestHelper.getTestPackagePath( DisableExecutableValidationInXmlTest.class ) + "validation-disable-executable-validation.xml",
						"META-INF/validation.xml"
				)
				.addAsManifestResource( "beans.xml" );
	}

	@Inject
	Repeater<String> repeater;

	@Test
	public void testExecutableValidationDisabled() throws Exception {
		repeater.reverse( null );
	}
}
