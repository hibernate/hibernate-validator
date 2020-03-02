/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import static org.testng.Assert.fail;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import org.hibernate.validator.test.util.TestHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	Repeater<String> repeater;

	@Test
	public void testExecutableValidationDisabled() throws Exception {
		try {
			repeater.reverse( null );
		}
		catch (ConstraintViolationException e) {
			fail( "CDI method interception should be disabled" );
		}
	}
}
