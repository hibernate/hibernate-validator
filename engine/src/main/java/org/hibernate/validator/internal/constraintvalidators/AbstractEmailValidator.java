/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.lang.annotation.Annotation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.util.DomainNameUtil;

/**
 * Checks that a given character sequence (e.g. string) is a well-formed email address.
 * <p>
 * The specification of a valid email can be found in
 * <a href="http://www.faqs.org/rfcs/rfc2822.html">RFC 2822</a>
 * and one can come up with a regular expression matching <a href="http://www.ex-parrot.com/~pdw/Mail-RFC822-Address.html">
 * all valid email addresses</a> as per specification. However, as this
 * <a href="http://www.regular-expressions.info/email.html">article</a> discusses it is not necessarily practical to
 * implement a 100% compliant email validator. This implementation is a trade-off trying to match most email while ignoring
 * for example emails with double quotes or comments.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class AbstractEmailValidator<A extends Annotation> implements ConstraintValidator<A, CharSequence> {

	private static final int MAX_LOCAL_PART_LENGTH = 64;

	private static final String LOCAL_PART_ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~\u0080-\uFFFF-]";
	private static final String LOCAL_PART_INSIDE_QUOTES_ATOM = "([a-z0-9!#$%&'*.(),<>\\[\\]:;  @+/=?^_`{|}~\u0080-\uFFFF-]|\\\\\\\\|\\\\\\\")";
	/**
	 * Regular expression for the local part of an email address (everything before '@')
	 */
	private static final Pattern LOCAL_PART_PATTERN = Pattern.compile(
			"(" + LOCAL_PART_ATOM + "+|\"" + LOCAL_PART_INSIDE_QUOTES_ATOM + "+\")" +
					"(\\." + "(" + LOCAL_PART_ATOM + "+|\"" + LOCAL_PART_INSIDE_QUOTES_ATOM + "+\")" + ")*", CASE_INSENSITIVE
	);

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}

		// cannot split email string at @ as it can be a part of quoted local part of email.
		// so we need to split at a position of last @ present in the string:
		String stringValue = value.toString();
		int splitPosition = stringValue.lastIndexOf( '@' );

		// need to check if
		if ( splitPosition < 0 ) {
			return false;
		}

		String localPart = stringValue.substring( 0, splitPosition );
		String domainPart = stringValue.substring( splitPosition + 1 );

		if ( !isValidEmailLocalPart( localPart ) ) {
			return false;
		}

		return DomainNameUtil.isValidEmailDomainAddress( domainPart );
	}

	private boolean isValidEmailLocalPart(String localPart) {
		if ( localPart.length() > MAX_LOCAL_PART_LENGTH ) {
			return false;
		}
		Matcher matcher = LOCAL_PART_PATTERN.matcher( localPart );
		return matcher.matches();
	}
}
