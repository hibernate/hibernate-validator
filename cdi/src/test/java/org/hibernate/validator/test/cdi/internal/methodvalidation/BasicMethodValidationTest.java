/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import static org.testng.Assert.fail;

import jakarta.enterprise.inject.Instance;
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
public class BasicMethodValidationTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Repeater.class )
				.addClass( DefaultRepeater.class )
				.addClass( Broken.class )
				.addClass( BrokenRepeaterImpl.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	Repeater<String> repeater;

	@Inject
	@Broken
	Instance<Repeater<String>> repeaterInstance;

	@Test
	public void testConstructorValidation() throws Exception {
		try {
			repeaterInstance.get();
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch (ConstraintViolationException e) {
			// success
		}
	}

	@Test
	public void testReturnValueValidation() throws Exception {
		try {
			repeater.reverse( null );
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch (ConstraintViolationException e) {
			// success
		}
	}

	@Test
	public void testParameterValidation() throws Exception {
		try {
			repeater.repeat( null );
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch (ConstraintViolationException e) {
			// success
		}
	}

	@Test
	public void testGetterValidation() throws Exception {
		try {
			repeater.getHelloWorld();
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch (ConstraintViolationException e) {
			// success
		}
	}
}
