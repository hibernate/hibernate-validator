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
package org.hibernate.validator.test.internal.cdi.methodvalidation;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class BasicMethodValidationTest {

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
	Repeater repeater;

	@Inject
	@Broken
	Instance<Repeater> repeaterInstance;

	@Test
	public void testConstructorValidation() throws Exception {
		try {
			repeaterInstance.get();
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch ( ConstraintViolationException e ) {
			// success
		}
	}

	@Test
	public void testReturnValueValidation() throws Exception {
		try {
			repeater.reverse( null );
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch ( ConstraintViolationException e ) {
			// success
		}
	}

	@Test
	public void testParameterValidation() throws Exception {
		try {
			repeater.repeat( null );
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch ( ConstraintViolationException e ) {
			// success
		}
	}

	@Test
	public void testGetterValidation() throws Exception {
		try {
			repeater.getHelloWorld();
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch ( ConstraintViolationException e ) {
			// success
		}
	}
}
