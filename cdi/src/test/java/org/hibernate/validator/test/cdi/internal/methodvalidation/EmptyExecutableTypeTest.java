/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class EmptyExecutableTypeTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Snafu.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	Snafu snafu;

	@Test
	public void testEmptyExecutableTypeParameterIsTreatedAsExecutableTypeNone() throws Exception {
		try {
			assertThat( snafu.foo() ).isNull();
		}
		catch (ConstraintViolationException e) {
			fail( "CDI method interceptor should not throw an exception" );
		}
	}
}
