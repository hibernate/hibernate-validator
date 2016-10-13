/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NumberPattern;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Checks that given {@link Number} matches provided regexp.
 *
 * @author Marko Bekhta
 */
public class NumberPatternValidator implements ConstraintValidator<NumberPattern, Number> {

	private static final Log log = LoggerFactory.make();

	private Pattern pattern;

	private String numberFormatString;

	@Override
	public void initialize(NumberPattern parameters) {
		NumberPattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for ( NumberPattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		try {
			this.pattern = java.util.regex.Pattern.compile( parameters.regexp(), intFlag );
		}
		catch (PatternSyntaxException e) {
			throw log.getInvalidRegularExpressionException( e );
		}
		this.numberFormatString = parameters.numberFormat();

		if ( !StringHelper.isNullOrEmptyString( numberFormatString ) ) {
			try {
				new DecimalFormat( numberFormatString );
			}
			catch (IllegalArgumentException e) {
				throw log.getInvalidNumberFormatException();
			}
		}

	}

	@Override
	public boolean isValid(Number value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		Matcher m = pattern.matcher( getCharSequenceRepresentation( value ) );
		return m.matches();
	}

	/**
	 * Converts given {@link Number} value to string representation to which regexp will be applied.
	 * Uses number format if one was provided.
	 *
	 * @param number value which is validated
	 *
	 * @return {@link String} representation of given value
	 */
	private String getCharSequenceRepresentation(Number number) {
		if ( StringHelper.isNullOrEmptyString( numberFormatString ) ) {
			//no number format was provided.
			if ( number instanceof BigDecimal ) {
				return ( (BigDecimal) number ).toPlainString();
			}
			return number.toString();
		}
		else {
			return new DecimalFormat( numberFormatString ).format( number );
		}

	}

}
