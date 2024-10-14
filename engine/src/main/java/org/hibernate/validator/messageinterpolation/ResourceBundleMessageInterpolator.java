/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.messageinterpolation;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import jakarta.el.ELManager;
import jakarta.el.ExpressionFactory;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.util.actions.GetClassLoader;
import org.hibernate.validator.internal.util.actions.SetContextClassLoader;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import com.sun.el.ExpressionFactoryImpl;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Adam Stawicki
 * @author Guillaume Smet
 */
public class ResourceBundleMessageInterpolator extends AbstractMessageInterpolator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ExpressionFactory expressionFactory;

	public ResourceBundleMessageInterpolator() {
		this( Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator) {
		this( userResourceBundleLocator, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator) {
		this( userResourceBundleLocator, contributorResourceBundleLocator, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			boolean cachingEnabled) {
		this( userResourceBundleLocator, contributorResourceBundleLocator, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(),
				false, cachingEnabled );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, boolean cachingEnabled) {
		this( userResourceBundleLocator, null, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false, cachingEnabled );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			boolean cachingEnabled,
			ExpressionFactory expressionFactory) {
		this( userResourceBundleLocator, null, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false, cachingEnabled );
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ResourceBundleMessageInterpolator(Set<Locale> locales, Locale defaultLocale, LocaleResolver localeResolver, boolean preloadResourceBundles) {
		super( locales, defaultLocale, localeResolver, preloadResourceBundles );
		this.expressionFactory = buildExpressionFactory();
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			Set<Locale> locales,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles) {
		super( userResourceBundleLocator, locales, defaultLocale, localeResolver, preloadResourceBundles );
		this.expressionFactory = buildExpressionFactory();
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			Set<Locale> locales,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles) {
		super( userResourceBundleLocator, contributorResourceBundleLocator, locales, defaultLocale, localeResolver, preloadResourceBundles );
		this.expressionFactory = buildExpressionFactory();
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			Set<Locale> locales,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles,
			boolean cachingEnabled) {
		super( userResourceBundleLocator, contributorResourceBundleLocator, locales, defaultLocale, localeResolver, preloadResourceBundles,
				cachingEnabled );
		this.expressionFactory = buildExpressionFactory();
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			Set<Locale> locales,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles,
			boolean cachingEnabled) {
		super( userResourceBundleLocator, null, locales, defaultLocale, localeResolver, preloadResourceBundles, cachingEnabled );
		this.expressionFactory = buildExpressionFactory();
	}

	/**
	 * @since 6.1.1
	 */
	@Incubating
	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			Set<Locale> locales,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles,
			boolean cachingEnabled,
			ExpressionFactory expressionFactory) {
		super( userResourceBundleLocator, null, locales, defaultLocale, localeResolver, preloadResourceBundles, cachingEnabled );
		this.expressionFactory = expressionFactory;
	}

	@Override
	protected String interpolate(Context context, Locale locale, String term) {
		InterpolationTerm expression = new InterpolationTerm( term, locale, expressionFactory );
		return expression.interpolate( context );
	}

	/**
	 * The jakarta.el FactoryFinder uses the TCCL to load the {@link ExpressionFactory} implementation so we need to be
	 * extra careful when initializing it.
	 *
	 * @return the {@link ExpressionFactory}
	 */
	private static ExpressionFactory buildExpressionFactory() {
		// First, we try to load the instance from the original TCCL.
		if ( canLoadExpressionFactory() ) {
			ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
			LOG.debug( "Loaded expression factory via original TCCL" );
			return expressionFactory;
		}

		final ClassLoader originalContextClassLoader = GetClassLoader.fromContext();

		try {
			// Then we try the Hibernate Validator class loader. In a fully-functional modular environment such as
			// WildFly or Jigsaw, it is the way to go.
			SetContextClassLoader.action( ResourceBundleMessageInterpolator.class.getClassLoader() );

			if ( canLoadExpressionFactory() ) {
				ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
				LOG.debug( "Loaded expression factory via HV classloader" );
				return expressionFactory;
			}

			// We try the CL of the EL module itself; the EL RI uses the TCCL to load the implementation from
			// its own module, so this should work.
			SetContextClassLoader.action( ELManager.class.getClassLoader() );
			if ( canLoadExpressionFactory() ) {
				ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
				LOG.debug( "Loaded expression factory via EL classloader" );
				return expressionFactory;
			}

			// Finally we try the CL of the EL implementation itself. This is necessary for OSGi now that the
			// implementation is separated from the API.
			SetContextClassLoader.action( ExpressionFactoryImpl.class.getClassLoader() );
			if ( canLoadExpressionFactory() ) {
				ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
				LOG.debug( "Loaded expression factory via com.sun.el classloader" );
				return expressionFactory;
			}
		}
		catch (Throwable e) {
			throw LOG.getUnableToInitializeELExpressionFactoryException( e );
		}
		finally {
			SetContextClassLoader.action( originalContextClassLoader );
		}

		// HV-793 - We fail eagerly in case we have no EL dependencies on the classpath
		throw LOG.getUnableToInitializeELExpressionFactoryException( null );
	}

	/**
	 * Instead of testing the different class loaders via {@link ELManager}, we directly access the
	 * {@link ExpressionFactory}. This avoids issues with loading the {@code ELUtil} class (used by {@code ELManager})
	 * after a failed attempt.
	 */
	private static boolean canLoadExpressionFactory() {
		try {
			ExpressionFactory.newInstance();
			return true;
		}
		catch (Throwable e) {
			LOG.debugv( e, "Failed to load expression factory via classloader {0}", GetClassLoader.fromContext() );
			return false;
		}
	}
}
