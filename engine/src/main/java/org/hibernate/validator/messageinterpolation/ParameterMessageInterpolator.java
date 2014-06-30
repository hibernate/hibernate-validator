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

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.ParameterTermResolver;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Resource bundle message interpolator, it does not support EL expression
 * and does support parameter value expression
 *
 * @author Adam Stawicki
 * @since 5.2
 */
public class ParameterMessageInterpolator extends AbstractMessageInterpolator {

	private static final Log log = LoggerFactory.make();

	public ParameterMessageInterpolator() {
		log.creationOfParameterMessageInterpolation();
	}

	@Override
	public String interpolate(Context context, Locale locale, String term) {
		if ( InterpolationTerm.isElExpression( term ) ) {
			log.getElUnsupported( term );
			return term;
		}
		else {
			ParameterTermResolver parameterTermResolver = new ParameterTermResolver();
			return parameterTermResolver.interpolate( context, term );
		}
	}

}
