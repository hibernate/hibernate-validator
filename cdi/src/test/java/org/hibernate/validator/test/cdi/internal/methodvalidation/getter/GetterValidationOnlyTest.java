/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

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
public class GetterValidationOnlyTest extends Arquillian {

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
			assertThat( onlyGetterValidated.foo() ).isNull();
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
