/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.messageinterpolation;

import java.util.Locale;

import org.hibernate.validator.internal.engine.messageinterpolation.ElInterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.ParameterInterpolationTerm;
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
 */
public class ResourceBundleMessageInterpolator extends AbstractMessageInterpolator {
	private static final Log log = LoggerFactory.make();
	
	public ResourceBundleMessageInterpolator(ResourceBundleLocator defaultResourceBundleLocator) {
		this(defaultResourceBundleLocator, true);
	}

	public ResourceBundleMessageInterpolator() {
		super();
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator testResourceBundleLocator, boolean cachingEnabled) {
		super(testResourceBundleLocator, cachingEnabled);
	}

	@Override
	public String interpolateExpressionLanguageTerm(Context context, String term, Locale locale) {
		InterpolationTerm expression = new ElInterpolationTerm( term, locale );
		return expression.interpolate( context );
	}

	@Override
	public String interpolateConstraintAnnotationValue(Context context, String term) {
		InterpolationTerm expression = new ParameterInterpolationTerm( term );
		return expression.interpolate( context );
	}
	
	
}
