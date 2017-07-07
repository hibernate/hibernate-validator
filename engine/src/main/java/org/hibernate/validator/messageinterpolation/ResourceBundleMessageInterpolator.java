/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
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

	private static final Log LOG = LoggerFactory.make();

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
		final ClassLoader originalContextClassLoader = run( GetClassLoader.fromContext() );

		List<Throwable> suppressedExceptions = new ArrayList<>();

		// First, we try to load the instance from the original TCCL.
		try {
			return ELManager.getExpressionFactory();
		}
		catch (Throwable e) {
			suppressedExceptions.add( e );
		}

		// Then we try the Hibernate Validator class loader. In a fully-functional modular environment such as
		// WildFly or Jigsaw, it is the way to go.

		try {
			run( SetContextClassLoader.action( ResourceBundleMessageInterpolator.class.getClassLoader() ) );
			return ELManager.getExpressionFactory();
		}
		catch (Throwable e) {
			suppressedExceptions.add( e );
		}
		finally {
			run( SetContextClassLoader.action( originalContextClassLoader ) );
		}

		// And in last resort, this is for OSGi environments and due to the fact that the ExpressionFactory
		// initialization is buggy, we set the TCCL to the class loader used to load a javax.el class.

		try {
			run( SetContextClassLoader.action( ELManager.class.getClassLoader() ) );
			return ELManager.getExpressionFactory();
		}
		catch (Throwable e) {
			for ( Throwable suppressedException : suppressedExceptions ) {
				e.addSuppressed( suppressedException );
			}

			// HV-793 - We fail eagerly in case we have no EL dependencies on the classpath
			throw LOG.getUnableToInitializeELExpressionFactoryException( e );
		}
		finally {
			run( SetContextClassLoader.action( originalContextClassLoader ) );
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
