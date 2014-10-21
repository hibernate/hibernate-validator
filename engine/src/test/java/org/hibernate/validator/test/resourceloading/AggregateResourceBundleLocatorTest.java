/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.resourceloading;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.testng.annotations.Test;

import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Test for {@link org.hibernate.validator.resourceloading.AggregateResourceBundleLocator}.
 *
 * @author Gunnar Morling
 */
public class AggregateResourceBundleLocatorTest {

	private static final String BUNDLE_NAME_1 =
			AggregateResourceBundleLocatorTest.class.getPackage()
					.getName() + ".AggregateResourceBundleLocatorTestBundle1";

	private static final String BUNDLE_NAME_2 =
			AggregateResourceBundleLocatorTest.class.getPackage()
					.getName() + ".AggregateResourceBundleLocatorTestBundle2";

	@Test
	public void valuesAreRetrievedFromBothSourceBundles() {

		ResourceBundleLocator locator =
				new AggregateResourceBundleLocator( Arrays.asList( BUNDLE_NAME_1, BUNDLE_NAME_2 ) );

		ResourceBundle resourceBundle = locator.getResourceBundle( Locale.getDefault() );

		assertNotNull( resourceBundle );

		//contained in bundle 1
		assertEquals( resourceBundle.getString( "key_1" ), "value 1 from bundle 1" );

		//contained in both bundles, bundle 1 comes first
		assertEquals( resourceBundle.getString( "key_2" ), "value 2 from bundle 1" );

		//contained in bundle 2
		assertEquals( resourceBundle.getString( "key_3" ), "value 3 from bundle 2" );
	}

	@Test
	public void valuesAreRetrievedFromDelegate() {

		ResourceBundleLocator locator =
				new AggregateResourceBundleLocator(
						Arrays.asList( BUNDLE_NAME_1 ),
						new PlatformResourceBundleLocator( BUNDLE_NAME_2 )
				);

		ResourceBundle resourceBundle = locator.getResourceBundle( Locale.ENGLISH );

		assertNotNull( resourceBundle );

		//contained in bundle 1
		assertEquals( resourceBundle.getString( "key_1" ), "value 1 from bundle 1" );

		//contained in both bundles, but bundle 1 is queried before bundle 2 (delegate)
		assertEquals( resourceBundle.getString( "key_2" ), "value 2 from bundle 1" );

		//contained in bundle 2
		assertEquals( resourceBundle.getString( "key_3" ), "value 3 from bundle 2" );
	}

	@Test
	public void nullReturnedAsBundleDoesNotExist() {

		ResourceBundleLocator locator = new AggregateResourceBundleLocator( Arrays.asList( "foo" ) );
		ResourceBundle resourceBundle = locator.getResourceBundle( Locale.ENGLISH );

		assertNull( resourceBundle );
	}
}
