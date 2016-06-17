/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation.getter;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class GetterValidationOnlyTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( OnlyGetterValidated.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	OnlyGetterValidated onlyGetterValidated;

	@Test
	public void testNonGetterValidationDoesNotOccur() throws Exception {
		try {
			assertNull( onlyGetterValidated.foo() );
		}
		catch (ConstraintViolationException e) {
			fail( "CDI method interceptor should not throw an exception" );
		}
	}

	@Test
	public void testGetterValidationOccurs() throws Exception {
		try {
			onlyGetterValidated.getFoo();
			fail( "CDI method interceptor should throw an exception" );
		}
		catch (ConstraintViolationException e) {
			// success
		}
	}
}
