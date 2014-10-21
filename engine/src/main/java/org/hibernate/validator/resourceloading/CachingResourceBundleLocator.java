/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.resourceloading;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * A {@link ResourceBundleLocator} implementation that wraps around another
 * locator and caches values retrieved from that locator.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class CachingResourceBundleLocator extends DelegatingResourceBundleLocator {

	private final ConcurrentMap<Locale, ResourceBundle> bundleCache = new ConcurrentHashMap<Locale, ResourceBundle>();

	/**
	 * Creates a new CachingResourceBundleLocator.
	 *
	 * @param delegate The locator from which the values actually will be retrieved.
	 */
	public CachingResourceBundleLocator(ResourceBundleLocator delegate) {
		super( delegate );
	}

	public ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle cachedResourceBundle = bundleCache.get( locale );
		if ( cachedResourceBundle == null ) {
			final ResourceBundle bundle = super.getResourceBundle( locale );
			if ( bundle != null ) {
				cachedResourceBundle = bundleCache.putIfAbsent( locale, bundle );
				if ( cachedResourceBundle == null ) {
					return bundle;
				}
			}
		}
		return cachedResourceBundle;
	}
}
