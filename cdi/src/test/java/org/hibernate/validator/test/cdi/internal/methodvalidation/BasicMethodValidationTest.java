/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
				.addAsManifestResource( "beans.xml" );
	}

	@Inject
	Repeater<String> repeater;

	@Inject
	@Broken
	Instance<Repeater<String>> repeaterInstance;

	@Test
	public void testConstructorValidation() throws Exception {
		assertThatThrownBy( () -> repeaterInstance.get() )
				.isInstanceOf( ConstraintViolationException.class );
	}

	@Test
	public void testReturnValueValidation() throws Exception {
		assertThatThrownBy( () -> repeater.reverse( null ) )
				.isInstanceOf( ConstraintViolationException.class );
	}

	@Test
	public void testParameterValidation() throws Exception {
		assertThatThrownBy( () -> repeater.repeat( null ) )
				.isInstanceOf( ConstraintViolationException.class );
	}

	@Test
	public void testGetterValidation() throws Exception {
		assertThatThrownBy( () -> repeater.getHelloWorld() )
				.isInstanceOf( ConstraintViolationException.class );
	}
}
