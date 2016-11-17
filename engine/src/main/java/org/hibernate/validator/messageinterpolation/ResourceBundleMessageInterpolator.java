/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import java.util.Locale;

import javax.el.ExpressionFactory;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
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

	private static ExpressionFactory buildExpressionFactory() {
		final ExpressionFactory moduleLoadingAttempt = attemptLoadingFromModularCL();
		if ( moduleLoadingAttempt != null ) {
			//This approach is expected to work in sane modular environments,
			//such as Jigsaw and WildFly
			return moduleLoadingAttempt;
		}
		else {
			//While this is an attempt to maintain compatibility
			//with some OSGi environments
			return attemptLoadingFromCurrentCL();
		}
	}

	private static ExpressionFactory attemptLoadingFromCurrentCL() {
		try {
			return ExpressionFactory.newInstance();
		}
		catch (Throwable e) {
			// HV-793 - We fail eagerly in case we have no EL dependencies on the classpath
			throw LOG.getUnableToInitializeELExpressionFactoryException( e );
		}
	}

	private static ExpressionFactory attemptLoadingFromModularCL() {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader originalContextClassLoader = currentThread.getContextClassLoader();
		try {
			// The javax.el FactoryFinder uses the TCCL to load the ExpressionFactory.
			// We need to be sure the ExpressionFactory implementation is visible to the class loader
			// so we set the TCCL to the class loader used to load this very class.
			currentThread.setContextClassLoader( ResourceBundleMessageInterpolator.class.getClassLoader() );
			return ExpressionFactory.newInstance();
		}
		catch (Throwable e) {
			//Ignoring this one, we'll try a different strategy
			return null;
		}
		finally {
			currentThread.setContextClassLoader( originalContextClassLoader );
		}
	}

}
