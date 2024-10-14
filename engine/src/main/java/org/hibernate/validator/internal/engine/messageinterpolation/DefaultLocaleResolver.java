/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Locale;

import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;

public class DefaultLocaleResolver implements LocaleResolver {

	@Override
	public Locale resolve(LocaleResolverContext context) {
		return context.getDefaultLocale();
	}
}
