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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class BeanMetaDataManagerTest {
	private static final Log log = LoggerFactory.make();
	// high enough to force a OutOfMemoryError in case references are not freed
	private static final int MAX_ENTITY_COUNT = 100000;

	private BeanMetaDataManager metaDataManager;

	@BeforeMethod
	public void setUpBeanMetaDataManager() {
		metaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				new ExecutableHelper( new TypeResolutionHelper() )
		);
	}

	@Test(enabled = false, description = "Disabled as it shows false failures too often. Run on demand if required")
	public void testBeanMetaDataCanBeGarbageCollected() throws Exception {
		Class<?> lastIterationsBean = null;
		int totalCreatedMetaDataInstances = 0;
		int cachedBeanMetaDataInstances = 0;
		for ( int i = 0; i < MAX_ENTITY_COUNT; i++ ) {
			Class<?> c = new CustomClassLoader().loadClass( Engine.class.getName() );
			BeanMetaData<?> meta = metaDataManager.getBeanMetaData( c );
			assertNotSame( meta.getBeanClass(), lastIterationsBean, "The classes should differ in each iteration" );
			lastIterationsBean = meta.getBeanClass();
			totalCreatedMetaDataInstances++;
			cachedBeanMetaDataInstances = metaDataManager.numberOfCachedBeanMetaDataInstances();

			if ( cachedBeanMetaDataInstances < totalCreatedMetaDataInstances ) {
				log.debug( "Garbage collection occurred and some metadata instances got garbage collected!" );
				log.debug( "totalCreatedMetaDataInstances:" + totalCreatedMetaDataInstances );
				log.debug( "cachedBeanMetaDataInstances:" + cachedBeanMetaDataInstances );
				break;
			}
		}

		if ( cachedBeanMetaDataInstances >= totalCreatedMetaDataInstances ) {
			fail( "Metadata instances should be garbage collectible" );
		}
	}

	@Test
	public void testIsConstrainedForConstrainedEntity() {
		assertTrue( metaDataManager.isConstrained( Engine.class ) );
	}

	@Test
	public void testIsConstrainedForUnConstrainedEntity() {
		assertFalse( metaDataManager.isConstrained( UnconstrainedEntity.class ) );
	}

	@Test
	public void testGetMetaDataForConstrainedEntity() {
		BeanMetaData<?> beanMetaData = metaDataManager.getBeanMetaData( Engine.class );
		assertTrue( beanMetaData instanceof BeanMetaDataImpl );
		assertTrue( beanMetaData.hasConstraints() );
	}

	@Test
	public void testGetMetaDataForUnConstrainedEntity() {
		assertFalse( metaDataManager.isConstrained( UnconstrainedEntity.class ) );

		BeanMetaData<?> beanMetaData = metaDataManager.getBeanMetaData( UnconstrainedEntity.class );
		assertTrue(
				beanMetaData instanceof BeanMetaDataImpl,
				"#getBeanMetaData should always return a valid BeanMetaData instance. Returned class: " + beanMetaData.getClass()
		);
		assertFalse( beanMetaData.hasConstraints() );
	}

	public class CustomClassLoader extends ClassLoader {

		/**
		 * Classes from this name space will be loaded by this class loader, all
		 * others will be loaded by the default loader.
		 */
		private static final String PACKAGE_PREFIX = "org.hibernate.validator.test";

		public CustomClassLoader() {
			super( CustomClassLoader.class.getClassLoader() );
		}

		@Override
		public Class<?> loadClass(String className) throws ClassNotFoundException {
			if ( className.startsWith( PACKAGE_PREFIX ) ) {
				return myLoadClass( className, true );
			}
			else {
				return super.loadClass( className );
			}
		}

		protected Class<?> myLoadClass(String name, boolean resolve) throws ClassNotFoundException {
			// make sure there is no parent delegation, instead call custom findClass
			Class<?> c = myFindClass( name );

			if ( resolve ) {
				resolveClass( c );
			}
			return c;
		}

		public Class<?> myFindClass(String className) {
			byte[] classByte;
			Class<?> result;

			try {
				String classPath = ClassLoader.getSystemResource(
						className.replace( '.', '/' ) + ".class"
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
			byte[] buff = new byte[size];
			FileInputStream fis = new FileInputStream( f );
			DataInputStream dis = new DataInputStream( fis );
			dis.readFully( buff );
			dis.close();
			return buff;
		}
	}

	public static class UnconstrainedEntity {
		private String foo;
	}
}
