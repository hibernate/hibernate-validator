/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
import java.util.Set;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.util.CollectionHelper;
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

	private final List<PlatformResourceBundleLocator> resourceBundleLocators;

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
		this( bundleNames, false, Collections.emptySet(), null );
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
		this( bundleNames, false, Collections.emptySet(), delegate, null );
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
	 * @param classLoader The classloader to use for loading the bundle.
	 * @since 5.2
	 */
	public AggregateResourceBundleLocator(List<String> bundleNames, ResourceBundleLocator delegate,
			ClassLoader classLoader) {
		this( bundleNames, false, Collections.emptySet(), delegate, classLoader );
	}

	/**
	 * Creates a locator that delivers a resource bundle merged from the given
	 * list of source bundles.
	 *
	 * @param bundleNames A list with source bundle names. The returned bundle will
	 * contain all entries from all source bundles. In case a key occurs
	 * in multiple source bundles, the value will be taken from the
	 * first bundle containing the key.
	 * @param preloadResourceBundles if resource bundles should be initialized when initializing the locator
	 * @param localesToInitialize The set of locales to initialize at bootstrap
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public AggregateResourceBundleLocator(List<String> bundleNames, boolean preloadResourceBundles, Set<Locale> localesToInitialize) {
		this( bundleNames, preloadResourceBundles, localesToInitialize, null );
	}

	/**
	 * Creates a locator that delivers a resource bundle merged from the given
	 * list of source bundles.
	 *
	 * @param bundleNames A list with source bundle names. The returned bundle will
	 * contain all keys from all source bundles. In case a key occurs
	 * in multiple source bundles, the value will be taken from the
	 * first bundle containing the key.
	 * @param preloadResourceBundles if resource bundles should be initialized when initializing the locator
	 * @param localesToInitialize The set of locales to initialize at bootstrap
	 * @param delegate A delegate resource bundle locator. The bundle returned by
	 * this locator will be added to the aggregate bundle after all
	 * source bundles.
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public AggregateResourceBundleLocator(List<String> bundleNames,
			boolean preloadResourceBundles,
			Set<Locale> localesToInitialize,
			ResourceBundleLocator delegate) {
		this( bundleNames, preloadResourceBundles, localesToInitialize, delegate, null );
	}

	/**
	 * Creates a locator that delivers a resource bundle merged from the given
	 * list of source bundles.
	 *
	 * @param bundleNames A list with source bundle names. The returned bundle will
	 * contain all keys from all source bundles. In case a key occurs
	 * in multiple source bundles, the value will be taken from the
	 * first bundle containing the key.
	 * @param preloadResourceBundles if resource bundles should be initialized when initializing the locator
	 * @param localesToInitialize The set of locales to initialize at bootstrap
	 * @param delegate A delegate resource bundle locator. The bundle returned by
	 * this locator will be added to the aggregate bundle after all
	 * source bundles.
	 * @param classLoader The classloader to use for loading the bundle.
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public AggregateResourceBundleLocator(List<String> bundleNames,
			boolean preloadResourceBundles,
			Set<Locale> localesToInitialize,
			ResourceBundleLocator delegate,
			ClassLoader classLoader) {
		super( delegate );
		Contracts.assertValueNotNull( bundleNames, "bundleNames" );

		List<PlatformResourceBundleLocator> tmpBundleLocators = new ArrayList<>( bundleNames.size() );
		for ( String bundleName : bundleNames ) {
			tmpBundleLocators
					.add( new PlatformResourceBundleLocator( bundleName, preloadResourceBundles ? localesToInitialize : Collections.emptySet(), classLoader ) );
		}
		this.resourceBundleLocators = CollectionHelper.toImmutableList( tmpBundleLocators );
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		List<ResourceBundle> sourceBundles = new ArrayList<ResourceBundle>();

		for ( PlatformResourceBundleLocator resourceBundleLocator : resourceBundleLocators ) {
			ResourceBundle resourceBundle = resourceBundleLocator.getResourceBundle( locale );

			if ( resourceBundle != null ) {
				sourceBundles.add( resourceBundle );
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
	 * <p>
	 * This class is package-private for the sake of testability.
	 * </p>
	 *
	 * @author Gunnar Morling
	 */
	public static class AggregateBundle extends ResourceBundle {
		private final Map<String, Object> contents = new HashMap<String, Object>();

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

		private final Iterator<T> source;

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

		@Override
		public boolean hasMoreElements() {
			return source.hasNext();
		}

		@Override
		public T nextElement() {
			return source.next();
		}
	}
}
