/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidator implements ConstraintValidator<Pattern, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final ConcurrentMap<String, java.util.regex.Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

	private java.util.regex.Pattern pattern;
	private String escapedRegexp;

	@Override
	public void initialize(Pattern parameters) {
		Pattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for ( Pattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		String patternCacheKey = parameters.regexp() + "_" + intFlag;
		pattern = PATTERN_CACHE.get( patternCacheKey );
		if ( pattern != null ) {
			escapedRegexp = InterpolationHelper.escapeMessageParameter( parameters.regexp() );
			return;
		}

		try {
			pattern = java.util.regex.Pattern.compile( parameters.regexp(), intFlag );
			PATTERN_CACHE.put( patternCacheKey, pattern );
		}
		catch (PatternSyntaxException e) {
			throw LOG.getInvalidRegularExpressionException( e );
		}

		escapedRegexp = InterpolationHelper.escapeMessageParameter( parameters.regexp() );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		if ( constraintValidatorContext instanceof HibernateConstraintValidatorContext ) {
			constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class ).addMessageParameter( "regexp", escapedRegexp );
		}

		Matcher m = pattern.matcher( value );
		return m.matches();
	}
}
