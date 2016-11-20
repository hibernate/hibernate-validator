/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

import javax.el.ExpressionFactory;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.SetClassLoader;
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
		this( (ClassLoader) null );
	}

	public ResourceBundleMessageInterpolator(ClassLoader externalClassLoader) {
		super();
		this.expressionFactory = buildExpressionFactory( getExpressionFactoryClassLoader( externalClassLoader ) );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator) {
		this( userResourceBundleLocator, (ClassLoader) null );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, ClassLoader externalClassLoader) {
		super( userResourceBundleLocator );
		this.expressionFactory = buildExpressionFactory( getExpressionFactoryClassLoader( externalClassLoader ) );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, ResourceBundleLocator contributorResourceBundleLocator) {
		this( userResourceBundleLocator, contributorResourceBundleLocator, (ClassLoader) null );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			ClassLoader externalClassLoader) {
		super( userResourceBundleLocator, contributorResourceBundleLocator );
		this.expressionFactory = buildExpressionFactory( getExpressionFactoryClassLoader( externalClassLoader ) );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, boolean cachingEnabled) {
		this( userResourceBundleLocator, null, cachingEnabled, (ClassLoader) null );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, boolean cachingEnabled, ClassLoader externalClassLoader) {
		this( userResourceBundleLocator, null, cachingEnabled, externalClassLoader );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			boolean cachingEnabled) {
		this( userResourceBundleLocator, contributorResourceBundleLocator, cachingEnabled, (ClassLoader) null );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			boolean cachingEnabled,
			ClassLoader externalClassLoader) {
		super( userResourceBundleLocator, contributorResourceBundleLocator, cachingEnabled );
		this.expressionFactory = buildExpressionFactory( getExpressionFactoryClassLoader( externalClassLoader ) );
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

	private static ClassLoader getExpressionFactoryClassLoader(ClassLoader externalClassLoader) {
		if ( externalClassLoader == null ) {
			return ResourceBundleMessageInterpolator.class.getClassLoader();
		}
		return externalClassLoader;
	}

	private static ExpressionFactory buildExpressionFactory(ClassLoader expressionFactoryClassLoader) {
		Contracts.assertNotNull( expressionFactoryClassLoader, MESSAGES.parameterMustNotBeNull( "expressionFactoryClassLoader" ) );

		final ClassLoader originalContextClassLoader = run( GetClassLoader.fromContext() );
		try {
			// The javax.el FactoryFinder uses the TCCL to load the ExpressionFactory.
			// We need to be sure the ExpressionFactory implementation is visible to the class loader
			// so we set the TCCL to the class loader used to load this very class.
			run( SetClassLoader.ofContext( expressionFactoryClassLoader ) );
			return ExpressionFactory.newInstance();
		}
		catch (Throwable e) {
			// HV-793 - We fail eagerly in case we have no EL dependencies on the classpath
			throw LOG.getUnableToInitializeELExpressionFactoryException( e );
		}
		finally {
			run( SetClassLoader.ofContext( originalContextClassLoader ) );
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
