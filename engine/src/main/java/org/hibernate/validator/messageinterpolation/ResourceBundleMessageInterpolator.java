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
		try {
			return ExpressionFactory.newInstance();
		}
		catch (Throwable e) {
			// HV-793 - We fail eagerly in case we have no EL dependencies on the classpath
			throw LOG.getUnableToInitializeELExpressionFactoryException( e );
		}
	}
}
