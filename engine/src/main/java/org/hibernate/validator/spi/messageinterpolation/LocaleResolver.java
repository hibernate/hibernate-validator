/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
