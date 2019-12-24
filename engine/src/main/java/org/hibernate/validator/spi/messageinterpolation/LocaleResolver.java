/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.messageinterpolation;

import java.util.Locale;

import org.hibernate.validator.Incubating;

/**
 * Define the strategy used to resolve the locale user for message interpolation when no locale is defined from the list
 * of supported locales.
 *
 * @author Guillaume Smet
 * @since 6.1.1
 */
@Incubating
public interface LocaleResolver {

	/**
	 * Returns the locale used for message interpolation when no locale is defined.
	 *
	 * @param context the context
	 *
	 * @return the locale to use for message interpolation, never null
	 */
	Locale resolve(LocaleResolverContext context);
}
