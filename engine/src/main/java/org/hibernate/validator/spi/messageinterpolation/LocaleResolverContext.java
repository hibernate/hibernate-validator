/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.messageinterpolation;

import java.util.Locale;
import java.util.Set;

import org.hibernate.validator.Incubating;

/**
 * Context used for locale resolution.
 *
 * @author Guillaume Smet
 * @since 6.1.1
 */
@Incubating
public interface LocaleResolverContext {

	Set<Locale> getSupportedLocales();

	Locale getDefaultLocale();
}
