/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import jakarta.inject.Inject;

import org.hibernate.validator.test.util.TestHelper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Hardy Ferentschik
 */
@ExtendWith(ArquillianExtension.class)
public class DisableExecutableValidationInXmlTest {
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
