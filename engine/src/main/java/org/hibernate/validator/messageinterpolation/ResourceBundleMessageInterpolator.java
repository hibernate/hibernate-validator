/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

import javax.el.ELManager;
import javax.el.ExpressionFactory;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

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
		super();
		this.expressionFactory = buildExpressionFactory();
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator) {
		super( userResourceBundleLocator );
		this.expressionFactory = buildExpressionFactory();
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator) {
		super( userResourceBundleLocator, contributorResourceBundleLocator );
		this.expressionFactory = buildExpressionFactory();
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			boolean cachingEnabled) {
		super( userResourceBundleLocator, contributorResourceBundleLocator, cachingEnabled );
		this.expressionFactory = buildExpressionFactory();
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, boolean cachingEnabled) {
		super( userResourceBundleLocator, null, cachingEnabled );
		this.expressionFactory = buildExpressionFactory();
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, boolean cachingEnabled, ExpressionFactory expressionFactory) {
		super( userResourceBundleLocator, null, cachingEnabled );
		this.expressionFactory = expressionFactory;
	}

	@Override
	public String interpolate(Context context, Locale locale, String term) {
		InterpolationTerm expression = new InterpolationTerm( term, locale, expressionFactory );
		return expression.interpolate( context );
	}

	/**
	 * The javax.el FactoryFinder uses the TCCL to load the {@link ExpressionFactory} implementation so we need to be
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

		final ClassLoader originalContextClassLoader = run( GetClassLoader.fromContext() );

		try {
			// Then we try the Hibernate Validator class loader. In a fully-functional modular environment such as
			// WildFly or Jigsaw, it is the way to go.
			run( SetContextClassLoader.action( ResourceBundleMessageInterpolator.class.getClassLoader() ) );

			if ( canLoadExpressionFactory() ) {
				ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
				LOG.debug( "Loaded expression factory via HV classloader" );
				return expressionFactory;
			}

			// Finally we try the CL of the EL module itself; the EL RI uses the TCCL to load the implementation from
			// its own module, so this should work
			run( SetContextClassLoader.action( ELManager.class.getClassLoader() ) );
			if ( canLoadExpressionFactory() ) {
				ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
				LOG.debug( "Loaded expression factory via EL classloader" );
				return expressionFactory;
			}
		}
		catch (Throwable e) {
			throw LOG.getUnableToInitializeELExpressionFactoryException( e );
		}
		finally {
			run( SetContextClassLoader.action( originalContextClassLoader ) );
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
			return false;
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
