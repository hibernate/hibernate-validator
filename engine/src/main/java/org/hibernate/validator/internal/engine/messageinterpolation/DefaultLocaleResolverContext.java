/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Locale;
import java.util.Set;

import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;

public class DefaultLocaleResolverContext implements LocaleResolverContext {

	private final Set<Locale> supportedLocales;

	private final Locale defaultLocale;

	public DefaultLocaleResolverContext(Set<Locale> supportedLocales, Locale defaultLocale) {
		this.supportedLocales = supportedLocales;
		this.defaultLocale = defaultLocale;
	}

	@Override
	public Set<Locale> getSupportedLocales() {
		return supportedLocales;
	}

	@Override
	public Locale getDefaultLocale() {
		return defaultLocale;
	}
}
