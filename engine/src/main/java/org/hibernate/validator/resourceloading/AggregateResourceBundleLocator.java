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
package org.hibernate.validator.resourceloading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * A {@link ResourceBundleLocator} implementation that provides access
 * to multiple source {@link ResourceBundle}s by merging them into one
 * aggregated bundle.
 *
 * @author Gunnar Morling
 */
public class AggregateResourceBundleLocator extends DelegatingResourceBundleLocator {
	private final List<String> bundleNames;

	/**
	 * Creates a locator that delivers a resource bundle merged from the given
	 * list of source bundles.
	 *
	 * @param bundleNames A list with source bundle names. The returned bundle will
	 * contain all entries from all source bundles. In case a key occurs
	 * in multiple source bundles, the value will be taken from the
	 * first bundle containing the key.
	 */
	public AggregateResourceBundleLocator(List<String> bundleNames) {
		this( bundleNames, null );
	}

	/**
	 * Creates a locator that delivers a resource bundle merged from the given
	 * list of source bundles.
	 *
	 * @param bundleNames A list with source bundle names. The returned bundle will
	 * contain all keys from all source bundles. In case a key occurs
	 * in multiple source bundles, the value will be taken from the
	 * first bundle containing the key.
	 * @param delegate A delegate resource bundle locator. The bundle returned by
	 * this locator will be added to the aggregate bundle after all
	 * source bundles.
	 */
	public AggregateResourceBundleLocator(List<String> bundleNames, ResourceBundleLocator delegate) {
		super( delegate );

		Contracts.assertValueNotNull( bundleNames, "bundleNames" );

		List<String> tmpBundleNames = new ArrayList<String>();
		tmpBundleNames.addAll( bundleNames );

		this.bundleNames = Collections.unmodifiableList( tmpBundleNames );
	}

	public ResourceBundle getResourceBundle(Locale locale) {
		List<ResourceBundle> sourceBundles = new ArrayList<ResourceBundle>();

		for ( String oneBundleName : bundleNames ) {
			ResourceBundleLocator oneLocator =
					new PlatformResourceBundleLocator( oneBundleName );

			ResourceBundle oneBundle = oneLocator.getResourceBundle( locale );

			if ( oneBundle != null ) {
				sourceBundles.add( oneBundle );
			}
		}

		ResourceBundle bundleFromDelegate = super.getResourceBundle( locale );

		if ( bundleFromDelegate != null ) {
			sourceBundles.add( bundleFromDelegate );
		}

		return sourceBundles.isEmpty() ? null : new AggregateBundle( sourceBundles );
	}

	/**
	 * A {@link ResourceBundle} whose content is aggregated from multiple source bundles.
	 * <p/>
	 * This class is package-private for the sake of testability.
	 *
	 * @author Gunnar Morling
	 */
	public static class AggregateBundle extends ResourceBundle {
		private Map<String, Object> contents = new HashMap<String, Object>();

		/**
		 * Creates a new AggregateBundle.
		 *
		 * @param bundles A list of source bundles, which shall be merged into one
		 * aggregated bundle. The newly created bundle will contain
		 * all keys from all source bundles. In case a key occurs in
		 * multiple source bundles, the value will be taken from the
		 * first bundle containing the key.
		 */
		public AggregateBundle(List<ResourceBundle> bundles) {
			if ( bundles != null ) {

				for ( ResourceBundle bundle : bundles ) {
					Enumeration<String> keys = bundle.getKeys();
					while ( keys.hasMoreElements() ) {
						String oneKey = keys.nextElement();
						if ( !contents.containsKey( oneKey ) ) {
							contents.put( oneKey, bundle.getObject( oneKey ) );
						}
					}
				}
			}
		}

		@Override
		public Enumeration<String> getKeys() {
			return new IteratorEnumeration<String>( contents.keySet().iterator() );
		}

		@Override
		protected Object handleGetObject(String key) {
			return contents.get( key );
		}
	}

	/**
	 * An {@link Enumeration} implementation, that wraps an {@link Iterator}. Can
	 * be used to integrate older APIs working with enumerations with iterators.
	 *
	 * @param <T> The enumerated type.
	 *
	 * @author Gunnar Morling
	 */
	private static class IteratorEnumeration<T> implements Enumeration<T> {

		private Iterator<T> source;

		/**
		 * Creates a new IterationEnumeration.
		 *
		 * @param source The source iterator. Must not be null.
		 */
		public IteratorEnumeration(Iterator<T> source) {

			if ( source == null ) {
				throw new IllegalArgumentException( "Source must not be null" );
			}

			this.source = source;
		}

		public boolean hasMoreElements() {
			return source.hasNext();
		}

		public T nextElement() {
			return source.next();
		}
	}
}
