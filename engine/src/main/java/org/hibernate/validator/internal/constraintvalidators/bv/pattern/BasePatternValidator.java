/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.pattern;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for any {@link Pattern} validators which prepares and compiles a regular expression.
 * In specific validators {@link BasePatternValidator#getCharSequenceRepresentation(Object)} method
 * should be overridden, which provides a string representation of a given value of type {@code T}.
 *
 * @author Hardy Ferentschik
 */
public abstract class BasePatternValidator<T> implements ConstraintValidator<Pattern, T> {

	private static final Log log = LoggerFactory.make();

	protected java.util.regex.Pattern pattern;

	@Override
	public void initialize(Pattern parameters) {
		Pattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for ( Pattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		try {
			pattern = java.util.regex.Pattern.compile( parameters.regexp(), intFlag );
		}
		catch (PatternSyntaxException e) {
			throw log.getInvalidRegularExpressionException( e );
		}
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		Matcher m = pattern.matcher( getCharSequenceRepresentation( value ) );
		return m.matches();
	}

	/**
	 * Converts given value of type {@code T} to a CharSequence to which regexp will be applied.
	 *
	 * @param value which is validated.
	 *
	 * @return {@link CharSequence} representation of given value.
	 */
	protected abstract CharSequence getCharSequenceRepresentation(T value);

}
