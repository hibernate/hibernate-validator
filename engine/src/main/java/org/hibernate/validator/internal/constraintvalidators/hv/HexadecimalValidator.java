/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Hexadecimal;
import org.hibernate.validator.constraints.Hexadecimal.HexPrefixes;
import org.hibernate.validator.constraints.Hexadecimal.HexStrictness;
import org.hibernate.validator.constraints.Hexadecimal.LetterCase;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Checks that a given character sequence (e.g. string) contains only hexadecimal characters.
 *
 * @since 9.2
 */
public class HexadecimalValidator implements ConstraintValidator<Hexadecimal, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private LetterCase letterCase;

	private HexStrictness strictness;

	private boolean allowEmpty;

	/**
	 * The literal prefix when the configured {@code prefix} is one of the well-known constants
	 * (matched exactly and case-sensitively).
	 */
	private String literalPrefix;

	/**
	 * The anchored prefix pattern when the configured {@code prefix} is a regular expression.
	 */
	private Pattern prefixPattern;

	@Override
	public void initialize(Hexadecimal constraintAnnotation) {
		letterCase = constraintAnnotation.letterCase();
		strictness = constraintAnnotation.strictness();
		allowEmpty = constraintAnnotation.allowEmpty();

		String prefix = constraintAnnotation.prefix();
		if ( prefix.isEmpty() ) {
			literalPrefix = null;
			prefixPattern = null;
		}
		else if ( HexPrefixes.HEX_LITERAL.equals( prefix ) || HexPrefixes.CSS_COLOR.equals( prefix ) ) {
			literalPrefix = prefix;
			prefixPattern = null;
		}
		else {
			literalPrefix = null;
			prefixPattern = compilePrefixPattern( prefix );
		}
	}

	private static Pattern compilePrefixPattern(String prefix) {
		if ( prefix.charAt( 0 ) == '^' ) {
			throw LOG.getPrefixCannotStartWithException( '^' );
		}
		if ( prefix.charAt( prefix.length() - 1 ) == '$' ) {
			throw LOG.getPrefixCannotEndWithException( '$' );
		}
		try {
			return Pattern.compile( "^" + prefix );
		}
		catch (PatternSyntaxException e) {
			throw LOG.getInvalidRegularExpressionException( e );
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		int start = 0;
		if ( literalPrefix != null ) {
			if ( !startsWith( value, literalPrefix ) ) {
				return false;
			}
			start = literalPrefix.length();
		}
		else if ( prefixPattern != null ) {
			Matcher matcher = prefixPattern.matcher( value );
			if ( !matcher.lookingAt() ) {
				return false;
			}
			start = matcher.end();
		}

		int length = value.length();
		if ( length == start ) {
			// The value is exactly the prefix, i.e. no hexadecimal characters are present.
			return allowEmpty;
		}

		for ( int i = start; i < length; i++ ) {
			char c = value.charAt( i );
			if ( !isHexDigit( c, strictness ) || !hasCorrectLetterCase( c, letterCase ) ) {
				return false;
			}
		}
		return true;
	}

	private static boolean startsWith(CharSequence value, String prefix) {
		if ( value.length() < prefix.length() ) {
			return false;
		}
		for ( int i = 0; i < prefix.length(); i++ ) {
			if ( value.charAt( i ) != prefix.charAt( i ) ) {
				return false;
			}
		}
		return true;
	}

	private static boolean isHexDigit(char c, HexStrictness strictness) {
		// The ASCII hex characters are always accepted.
		if ( ( c >= '0' && c <= '9' ) || ( c >= 'a' && c <= 'f' ) || ( c >= 'A' && c <= 'F' ) ) {
			return true;
		}
		if ( strictness == HexStrictness.LENIENT ) {
			// The fullwidth forms of the hex characters.
			return ( c >= '０' && c <= '９' )
					|| ( c >= 'ａ' && c <= 'ｆ' )
					|| ( c >= 'Ａ' && c <= 'Ｆ' );
		}
		return false;
	}

	private static boolean hasCorrectLetterCase(char c, LetterCase letterCase) {
		if ( letterCase == LetterCase.INSENSITIVE ) {
			return true;
		}
		// Digits carry no case and are always accepted; only the hex letters are subject to the case rule.
		boolean isLowerCase = ( c >= 'a' && c <= 'f' ) || ( c >= 'ａ' && c <= 'ｆ' );
		boolean isUpperCase = ( c >= 'A' && c <= 'F' ) || ( c >= 'Ａ' && c <= 'Ｆ' );
		if ( letterCase == LetterCase.LOWER_CASE ) {
			return !isUpperCase;
		}
		return !isLowerCase;
	}
}
