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
package org.hibernate.validator.test.internal.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.validation.Validation;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-793")
public class ValidatorFactoryNoELBootstrapTest {

	@Test
	public void testMissingELDependencyThrowsExceptionDuringFactoryBootstrap() throws Exception {
		ClassLoader classLoader = new ELIgnoringClassLoader();
		Class<?> clazz = classLoader.loadClass( Validation.class.getName() );

		Object validation = clazz.newInstance();
		Method m = clazz.getMethod( "buildDefaultValidatorFactory" );
		try {
			m.invoke( validation );
			fail( "An exception should have been thrown" );
		}
		catch ( InvocationTargetException e ) {
			Exception exceptionInValidation = (Exception) e.getTargetException();
			assertEquals(
					exceptionInValidation.getMessage(),
					"Unable to instantiate Configuration.",
					"Bootstrapping in Validation should throw an exception "
			);
			Throwable rootCause = exceptionInValidation.getCause();
			assertTrue(
					rootCause.getMessage().startsWith( "HV000183" ),
					"The root cause of the failure should be missing EL dependencies"
			);
		}
	}

	/**
	 * A lot of classpath magic is happening here. The test suite has the EL dependencies on the classpath.
	 * To test what happens if the EL classes cannot be found, we need to somehow "remove" these classes from the
	 * classpath.
	 */
	public class ELIgnoringClassLoader extends ClassLoader {
		private final String EL_PACKAGE_PREFIX = "javax.el";
		private final String[] PASS_THROUGH_PACKAGE_PREFIXES = new String[] {
				"java.",
				"javax.xml.",
				"sun.",
				"org.apache."
		};

		public ELIgnoringClassLoader() {
			super( ELIgnoringClassLoader.class.getClassLoader() );
		}

		@Override
		public Class<?> loadClass(String className) throws ClassNotFoundException {
			// This is what we in the end want to achieve. Throw ClassNotFoundException for javax.el classes
			if ( className.startsWith( EL_PACKAGE_PREFIX ) ) {
				throw new ClassNotFoundException();
			}
			// for all other classes we have to jump through some hoops
			else {
				// load the class via the parent class loader
				Class<?> clazz = super.loadClass( className );
				// if we have a class from the java name space we need to return it, since the security manager
				// won't allow to redefine it
				for ( String prefix : PASS_THROUGH_PACKAGE_PREFIXES ) {
					if ( className.startsWith( prefix ) ) {
						return clazz;
					}
				}
			}

			// for all other classes we load the class data from disk and define the class new
			// this will ensure that ELIgnoringClassLoader will be the associated classloader for this class
			byte[] classData;
			try {
				classData = loadClassData( className );
			}
			catch ( IOException e ) {
				throw new RuntimeException();
			}
			return defineClass( className, classData, 0, classData.length, null );
		}
	}

	private byte[] loadClassData(String className) throws IOException {
		String path = "/" + className.replace( ".", "/" ) + ".class";
		InputStream in = ValidatorFactoryNoELBootstrapTest.class.getResourceAsStream( path );
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int bytesRead;
		byte[] data = new byte[16384];
		while ( ( bytesRead = in.read( data, 0, data.length ) ) != -1 ) {
			buffer.write( data, 0, bytesRead );
		}

		buffer.flush();
		return buffer.toByteArray();
	}
}
