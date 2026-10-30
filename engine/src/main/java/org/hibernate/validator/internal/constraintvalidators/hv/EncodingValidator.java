/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Encoding;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Checks that every character of a given character sequence (e.g. string) can be encoded in a given charset.
 * <p>
 * The charset is resolved once, at initialization. {@code US-ASCII} and {@code ISO-8859-1} are validated without
 * any allocation, through a simple range check. For any other charset, a new {@link CharsetEncoder} is created per
 * validation, as encoders are not thread-safe while validator instances are shared; the encoder allocates an output
 * buffer proportional to the length of the value.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
public class EncodingValidator implements ConstraintValidator<Encoding, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private Charset charset;

	/**
	 * The exclusive upper bound of encodable characters, used to optimize validation for charsets that
	 * can be validated with a simple character-range check, or {@code -1} if this optimization does not apply.
	 */
	private int maxExclusiveChar;

	@Override
	public void initialize(Encoding constraintAnnotation) {
		var charsetName = constraintAnnotation.charset();
		// Resolve the charset once and fail fast on an unknown name, an illegal name or a decode-only charset,
		// so that a misconfigured constraint is reported at initialization rather than on every validation.
		try {
			charset = Charset.forName( charsetName );
		}
		catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
			throw LOG.getUnsupportedEncodingCharsetException( charsetName, e );
		}
		if ( !charset.canEncode() ) {
			throw LOG.getUnsupportedEncodingCharsetException( charsetName, null );
		}

		// US-ASCII and ISO-8859-1 map each char 1:1 to a code point below a fixed bound, so a range check is enough;
		// any other charset falls back to an actual CharsetEncoder.
		if ( StandardCharsets.US_ASCII.equals( charset ) ) {
			maxExclusiveChar = 0x80;
		}
		else if ( StandardCharsets.ISO_8859_1.equals( charset ) ) {
			maxExclusiveChar = 0x100;
		}
		else {
			maxExclusiveChar = -1;
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		if ( maxExclusiveChar != -1 ) {
			return isValidFastPath( value );
		}
		return charset.newEncoder().canEncode( value );
	}

	/**
	 * Validates the value without creating a {@link CharsetEncoder}, for charsets that map each {@code char}
	 * 1:1 to a code point below {@link #maxExclusiveChar} ({@code US-ASCII} and {@code ISO-8859-1}).
	 * <p>
	 * A new encoder would otherwise be needed for every validation, as encoders are not thread-safe while
	 * validator instances are shared, and encoding allocates an output buffer proportional to the length of
	 * the value. This check gives the same result without any allocation.
	 */
	private boolean isValidFastPath(CharSequence value) {
		for ( int i = 0; i < value.length(); i++ ) {
			if ( value.charAt( i ) >= maxExclusiveChar ) {
				return false;
			}
		}
		return true;
	}

}
