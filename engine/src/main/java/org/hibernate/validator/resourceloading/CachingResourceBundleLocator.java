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
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
