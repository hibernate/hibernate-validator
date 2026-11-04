/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.resourceloading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import org.junit.jupiter.api.Test;

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
		assertEquals( "value 1 from bundle 1", resourceBundle.getString( "key_1" ) );

		//contained in both bundles, bundle 1 comes first
		assertEquals( "value 2 from bundle 1", resourceBundle.getString( "key_2" ) );

		//contained in bundle 2
		assertEquals( "value 3 from bundle 2", resourceBundle.getString( "key_3" ) );
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
		assertEquals( "value 1 from bundle 1", resourceBundle.getString( "key_1" ) );

		//contained in both bundles, but bundle 1 is queried before bundle 2 (delegate)
		assertEquals( "value 2 from bundle 1", resourceBundle.getString( "key_2" ) );

		//contained in bundle 2
		assertEquals( "value 3 from bundle 2", resourceBundle.getString( "key_3" ) );
	}

	@Test
	public void nullReturnedAsBundleDoesNotExist() {

		ResourceBundleLocator locator = new AggregateResourceBundleLocator( Arrays.asList( "foo" ) );
		ResourceBundle resourceBundle = locator.getResourceBundle( Locale.ENGLISH );

		assertNull( resourceBundle );
	}
}
