/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.resourceloading;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>
 * Used by {@link org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator} to load resource bundles
 * containing message texts to be displayed in case of validation errors.
 * </p>
 * <p>
 * The default implementation provides access to the bundle "ValidationMessages"
 * as described in the BV specification. By providing additional implementations
 * of this interface, alternative ways of bundle loading can be realized, e.g.
 * by loading bundles based on XML files or from a database.
 * </p>
 * <p>
 * A {@code ResourceBundleLocator} implementation must be thread-safe.
 * </p>
 *
 * @author Gunnar Morling
 */
public interface ResourceBundleLocator {

	/**
	 * Returns a resource bundle for the given locale.
	 *
	 * @param locale A locale, for which a resource bundle shall be retrieved. Must
	 * not be null.
	 *
	 * @return A resource bundle for the given locale. May be null, if no such
	 *         bundle exists.
	 */
	ResourceBundle getResourceBundle(Locale locale);
}
