/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.resourceloading;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.testng.annotations.Test;

/**
 * Test for {@code AggregateResourceBundle}.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class AggregateResourceBundleTest {

	private static final String BUNDLE_NAME_1 = AggregateResourceBundleTest.class.getPackage()
			.getName() + ".AggregateResourceBundleTestBundle1";

	private static final String BUNDLE_NAME_2 = AggregateResourceBundleTest.class.getPackage()
			.getName() + ".AggregateResourceBundleTestBundle2";

	@Test
	public void aggregateBundleContainsKeysOfAllSourceBundles() {
		ResourceBundle bundle_1 = ResourceBundle.getBundle( BUNDLE_NAME_1 );
		ResourceBundle bundle_2 = ResourceBundle.getBundle( BUNDLE_NAME_2 );

		ResourceBundle aggregateBundle = new AggregateResourceBundle( Arrays.asList( bundle_1, bundle_2 ) );

		Set<String> actualKeys = getAsSet( aggregateBundle.getKeys() );
		Set<String> expectedKeys = new HashSet<String>( Arrays.asList( "key_1", "key_2", "key_3" ) );

		assertEquals( actualKeys, expectedKeys );
	}

	@Test
	public void aggregateBundleWithNoSourceBundlesContainsNoKeys() {
		ResourceBundle aggregateBundle = new AggregateResourceBundle( Collections.<ResourceBundle>emptyList() );
		assertTrue( getAsSet( aggregateBundle.getKeys() ).isEmpty() );
	}

	@Test
	public void valuesProperlyRetrievedFromAggregateBundle() {
		ResourceBundle bundle_1 = ResourceBundle.getBundle( BUNDLE_NAME_1 );
		ResourceBundle bundle_2 = ResourceBundle.getBundle( BUNDLE_NAME_2 );

		ResourceBundle aggregateBundle = new AggregateResourceBundle( Arrays.asList( bundle_1, bundle_2 ) );

		assertEquals(
				aggregateBundle.getString( "key_1" ),
				"value 1 from bundle 1",
				"Value for key_1 should be retrieved from bundle 1"
		);
		assertEquals(
				aggregateBundle.getString( "key_2" ),
				"value 2 from bundle 1",
				"Value for key_2 should be retrieved from bundle 1"
		);
		assertEquals(
				aggregateBundle.getString( "key_3" ),
				"value 3 from bundle 2",
				"Value for key_3 should be retrieved from bundle 2"
		);
	}

	private Set<String> getAsSet(Enumeration<String> e) {
		Set<String> theValue = new HashSet<String>();

		while ( e.hasMoreElements() ) {
			theValue.add( e.nextElement() );
		}

		return theValue;
	}
}
