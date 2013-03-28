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
public class ValidationOfInheritedMethodTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Greeter.class )
				.addClass( SimpleGreeter.class )
				.addClass( AbstractGreeter.class )
				.addClass( Encryptor.class )
				.addClass( RefusingEncryptor.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	Greeter greeter;

	@Inject
	Encryptor encryptor;

	@Test
	public void testInheritedMethodGetsValidated() throws Exception {
		try {
			greeter.greet( "how are you" );
			fail( "CDI method interceptor should throw an exception" );
		}
		catch ( ConstraintViolationException e ) {
			// success
		}
	}

	@Test
	public void testInterfaceMethodWithExecutableTypeNoneDoesNotGetValidated() throws Exception {
		try {
			assertNull( encryptor.encrypt( "top secret" ) );
		}
		catch ( ConstraintViolationException e ) {
			fail( "Encryptor#encrypt should not be validated, because it is explicitly excluded from executable validation" );
		}
	}
}
