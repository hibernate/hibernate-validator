/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.messageinterpolation;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.ParameterTermResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.TermInterpolator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;

/**
 * Resource bundle message interpolator, it does not support EL expression
 * and does support parameter value expression
 *
 * @author Adam Stawicki
 * @author Guillaume Smet
 * @since 5.2
 */
public class ParameterMessageInterpolator extends AbstractMessageInterpolator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	public ParameterMessageInterpolator() {
		this( Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false );
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ParameterMessageInterpolator(Set<Locale> locales, Locale defaultLocale, boolean preloadResourceBundles) {
		this( locales, defaultLocale, new DefaultLocaleResolver(), preloadResourceBundles );
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ParameterMessageInterpolator(Set<Locale> locales, Locale defaultLocale, LocaleResolver localeResolver, boolean preloadResourceBundles) {
		super( locales, defaultLocale, localeResolver, preloadResourceBundles );
	}

	@Override
	protected String interpolate(Context context, Locale locale, String term) {
		if ( TermInterpolator.isElExpression( term ) ) {
			LOG.warnElIsUnsupported( term );
			return term;
		}
		else {
			return ParameterTermResolver.INSTANCE.interpolate( context, locale, term );
		}
	}
}
