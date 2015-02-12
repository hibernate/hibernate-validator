/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.resourceloading;

import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * Abstract base for all {@link ResourceBundleLocator} implementations, that
 * wish to delegate to some other locator.
 *
 * @author Gunnar Morling
 */
public abstract class DelegatingResourceBundleLocator implements ResourceBundleLocator {

	private final ResourceBundleLocator delegate;

	/**
	 * Creates a new {@link DelegatingResourceBundleLocator}.
	 *
	 * @param delegate The delegate.
	 */
	public DelegatingResourceBundleLocator(ResourceBundleLocator delegate) {
		this.delegate = delegate;
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		return delegate == null ? null : delegate.getResourceBundle( locale );
	}
}
