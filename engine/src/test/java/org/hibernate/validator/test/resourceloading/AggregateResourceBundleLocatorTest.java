/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
