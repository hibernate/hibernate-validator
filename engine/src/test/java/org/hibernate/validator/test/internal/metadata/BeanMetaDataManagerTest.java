/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.metadata;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.testng.Assert.assertEquals;
import static org.testng.FileAssert.fail;


/**
 * @author Hardy Ferentschik
 */
public class BeanMetaDataManagerTest {
	private static final Log log = LoggerFactory.make();

	@Test
	public void testBeanMetaDataCanBeGarbageCollected() throws Exception {
		BeanMetaDataManager metaDataManager = new BeanMetaDataManager( new ConstraintHelper() );

		for ( int i = 0; i < 100; i++ ) {
			Class<?> c = new CustomClassLoader( Fubar.class.getName() ).loadClass( Fubar.class.getName() );
			metaDataManager.getBeanMetaData( c );
		}

		assertEquals( metaDataManager.numberOfCachedBeanMetaDataInstances(), 100 );

		try {
			byte[][] buf = new byte[1024][];
			for ( int i = 0; i < buf.length; i++ ) {
				buf[i] = new byte[10 * 1024 * 1024];
			}
			fail( "The byte array allocation should have triggered a OutOfMemoryError" );
		}
		catch ( OutOfMemoryError ex ) {
			log.debug( "Successfully forced a OutOfMemoryError" );
		}
		// not really reliable. if we get problems with this test we might have to disable it
		System.gc();

		assertEquals(
				metaDataManager.numberOfCachedBeanMetaDataInstances(), 64,
				"Only the hard referenced entries should be left. Soft references should have been garbage collected"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCustomMetaDataCacheSettings() throws Exception {
		BeanMetaDataManager metaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				Collections.<MetaDataProvider>emptyList(),
				1,
				10
		);

		for ( int i = 0; i < 100; i++ ) {
			Class<?> c = new CustomClassLoader( Fubar.class.getName() ).loadClass( Fubar.class.getName() );
			metaDataManager.getBeanMetaData( c );
		}

		assertEquals( metaDataManager.numberOfCachedBeanMetaDataInstances(), 10 );
	}

	public static class Fubar {
		@NotNull
		Object o;
	}

	public class CustomClassLoader extends ClassLoader {
		private final String className;

		public CustomClassLoader(String className) {
			super( CustomClassLoader.class.getClassLoader() );
			this.className = className;
		}

		public Class loadClass(String className) throws ClassNotFoundException {
			if ( this.className.equals( className ) ) {
				return myLoadClass( className, true );
			}
			else {
				return super.loadClass( className );
			}
		}

		protected Class<?> myLoadClass(String name, boolean resolve) throws ClassNotFoundException {
			// make sure there is no parent delegation, instead call custom findClass
			Class c = myFindClass( name );

			if ( resolve ) {
				resolveClass( c );
			}
			return c;
		}

		public Class myFindClass(String className) {
			byte classByte[];
			Class result;

			try {
				String classPath = ClassLoader.getSystemResource(
						className.replace( '.', File.separatorChar )
								+ ".class"
				).getFile();
				classByte = loadClassData( classPath );
				result = defineClass( className, classByte, 0, classByte.length, null );
				return result;
			}
			catch ( Exception e ) {
				return null;
			}
		}

		private byte[] loadClassData(String className) throws IOException {
			File f;
			f = new File( className );
			int size = (int) f.length();
			byte buff[] = new byte[size];
			FileInputStream fis = new FileInputStream( f );
			DataInputStream dis = new DataInputStream( fis );
			dis.readFully( buff );
			dis.close();
			return buff;
		}
	}
}


