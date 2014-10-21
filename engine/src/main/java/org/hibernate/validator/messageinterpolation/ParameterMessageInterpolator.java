/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
