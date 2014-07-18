/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.security;

import java.io.FilePermission;
import java.net.MalformedURLException;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.testng.Assert.assertTrue;

/**
 * Test that Hibernate Validator can run under a security manager
 * Also makes sure that the client code (test-classes/) cannot abuse and circumvent
 * Hibernate Validator security net
 *
 * @author Emmanuel Bernard <emmanuel@hibernate.org>
 */
public class SecurityManagerTest {
	private static final String TEST_CLASSES_DIR = "test-classes/";

	@Test
	public void testEnabledSecurityManager() throws Exception {
		//before we set the SM, we should be able to access ReflectionHelper
		AccessControlContext accessControlContext = ReflectionHelper.getAccessControlContext();
		ReflectionHelper.getDeclaredField( accessControlContext, ArrayList.class, "size" );
		assertTrue( true, "Without SecurityManager we should be able to access ReflectionHelper" );

		SecurityManager oldSecurityManager = System.getSecurityManager();
		Policy oldPolicy = Policy.getPolicy();
		Policy.setPolicy( new TestPolicy() );
		System.setSecurityManager( new SecurityManager() );
		try {
			Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
			Set<ConstraintViolation<Person>> violations = validator.validate( new Person() );

			assertTrue( violations.size() == 1, "Wrong number of violations" );
			try {
				ArrayList.class.getDeclaredField( "size" );
				assertTrue( false, "Should have raised an exception due to lacking permissions" );
			}
			catch (SecurityException e) {
				//expected
			}
			catch (NoSuchFieldException e) {
				assertTrue( false, "Should not happen, error in test." );
			}

			// Must not use the helper using an ACC representing ourselves
			// Also check that setting the SM *after* we had computed the ACC is fine
			try {
				accessControlContext = ReflectionHelper.getAccessControlContext();
				ReflectionHelper.getDeclaredField( accessControlContext, ArrayList.class, "size" );
				assertTrue( false, "Should have raised an exception due to lacking permissions" );
			}
			catch (SecurityException e) {
				//expected
			}

			// Forging an AccessControlContext should fail
			try {
				 accessControlContext = getAccessControlContext();
				ReflectionHelper.getDeclaredField( accessControlContext, ArrayList.class, "size" );
				assertTrue( false, "Should have raised an exception due to lacking permissions" );
			}
			catch (SecurityException e) {
				//expected
			}
			catch (MalformedURLException e) {
				assertTrue( false, "Should not happen, error in test." );
			}

		}
		finally {
			System.setSecurityManager( oldSecurityManager );
			Policy.setPolicy( oldPolicy );
		}
	}

	private static AccessControlContext getAccessControlContext() throws MalformedURLException {
		ProtectionDomain domain = new ProtectionDomain( new CodeSource(
				new java.net.URL( "http://hibernate.org/someramdomurlnotthetestclassesdirectory/" ),
				(Certificate[])null ), null );
		return new AccessControlContext( new ProtectionDomain[]{ domain } );
	}

	private class TestPolicy extends Policy {
		@Override
		public boolean implies(ProtectionDomain domain, Permission permission) {
			String location = domain.getCodeSource().getLocation().getPath().replaceAll( "\\\\", "/" );
			if ( permission.getName().equals( "setSecurityManager" ) ) {
				// let it go through, we need to set the SecurityManager back
				return true;
			}
			if ( permission.getName().equals( "java.specification.version" ) ) {
				//not sure what happens here but it is requested from the test-classes/ in ConfigurationImpl.getJavaRelease
				return true;
			}
			if ( permission instanceof FilePermission ) {
				return true;
			}

			if ( location.endsWith( TEST_CLASSES_DIR ) ) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	private static class Person {

		@NotNull
		private final String name = null;
	}
}
