/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.resourceloading;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.testng.Assert.assertEquals;


/**
 * @author Hardy Ferentschik
 */
public class PlatformResourceBundleLocatorTest {
	private static final String BASE_RESOURCE_NAME = "org/hibernate/validator/test/resourceloading/TestMessages";

	private PlatformResourceBundleLocator bundleLocator;

	@BeforeMethod
	public void setUp() {
		bundleLocator = new PlatformResourceBundleLocator(
				BASE_RESOURCE_NAME, new TestClassLoader( PlatformResourceBundleLocatorTest.class.getClassLoader() ),
				true
		);
	}

	@Test
	public void multiple_properties_files_can_be_aggregated() {
		ResourceBundle resourceBundle = bundleLocator.getResourceBundle( Locale.ROOT );
		assertEquals(
				resourceBundle.keySet().size(), 2, "There should be two keys since the root bundle is aggregated"
		);
		assertThat( resourceBundle.keySet() ).containsOnly( "foo", "bar" );
	}

	@Test
	public void aggregation_works_across_bundle_families() {
		ResourceBundle resourceBundle = bundleLocator.getResourceBundle( Locale.GERMAN );
		assertEquals(
				resourceBundle.keySet().size(), 3, "There should be two keys since the bundle is aggregated"
		);
		assertThat( resourceBundle.keySet() ).containsOnly( "foo", "bar", "snafu" );
	}

	@Test
	public void the_most_specific_key_value_pair_is_returned() {
		ResourceBundle resourceBundle = bundleLocator.getResourceBundle( Locale.GERMAN );
		assertEquals(
				resourceBundle.getString( "foo" ), "123_de",
				"The language specific version of the value should be retrieved"
		);
	}

	public class TestClassLoader extends URLClassLoader {
		private final Map<String, List<String>> mappedResources;

		public TestClassLoader(ClassLoader classLoader) {
			super( new URL[] { }, classLoader );

			this.mappedResources = newHashMap();
			mappedResources.put(
					BASE_RESOURCE_NAME + ".properties",
					Arrays.asList(
							"org/hibernate/validator/test/resourceloading/TestMessages1.properties",
							"org/hibernate/validator/test/resourceloading/TestMessages2.properties"
					)
			);
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			if ( mappedResources.containsKey( name ) ) {
				ArrayList<URL> urls = newArrayList();
				for ( String mappedResourceName : mappedResources.get( name ) ) {
					URL url = getResource( mappedResourceName );
					urls.add( url );
				}
				return Collections.enumeration( urls );
			}
			else {
				return super.getResources( name );
			}
		}
	}
}
