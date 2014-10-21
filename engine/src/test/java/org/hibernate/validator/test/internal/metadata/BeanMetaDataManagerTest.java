/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
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
	private static final int LOOP_COUNT = 100000;
	private static final int ARRAY_ALLOCATION_SIZE = 100000;

	private BeanMetaDataManager metaDataManager;

	@BeforeMethod
	public void setUpBeanMetaDataManager() {
		metaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				new ExecutableHelper( new TypeResolutionHelper() ),
				new DefaultParameterNameProvider(),
				Collections.<MetaDataProvider>emptyList()
		);
	}

	@Test(enabled = false, description = "Disabled as it shows false failures too often. Run on demand if required")
	public void testBeanMetaDataCanBeGarbageCollected() throws Exception {
		Class<?> lastIterationsBean = null;
		int totalCreatedMetaDataInstances = 0;
		int cachedBeanMetaDataInstances = 0;

		try {
			// help along the OutOfMemoryError by allocating extra memory and holding on to it in this list
			List<Object> memoryConsumer = new ArrayList<Object>();
			for ( int i = 0; i < LOOP_COUNT; i++ ) {
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
				memoryConsumer.add( new long[ARRAY_ALLOCATION_SIZE] );
			}
		}
		catch ( OutOfMemoryError e ) {
			log.debug( "Out of memory error occurred." );
			log.debug( "totalCreatedMetaDataInstances:" + totalCreatedMetaDataInstances );
			log.debug( "cachedBeanMetaDataInstances:" + cachedBeanMetaDataInstances );
		}

		// Before an OutOfMemoryError occurs soft references should be collected. If not all, at least some
		// of the cached instances should have been released.
		if ( !( cachedBeanMetaDataInstances < totalCreatedMetaDataInstances ) ) {
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
