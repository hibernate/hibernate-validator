/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.net.IDN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Email;

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
public class EmailValidator implements ConstraintValidator<Email, CharSequence> {
	private static final String LOCAL_PART_ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~\u0080-\uFFFF-]";
	private static final String DOMAIN_LABEL = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
	private static final String DOMAIN = DOMAIN_LABEL + "+(\\." + DOMAIN_LABEL + "+)*";
	private static final String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";
	private static final int MAX_LOCAL_PART_LENGTH = 64;
	/**
	 * This is the maximum length of a domain name. But be aware that each label (parts separated by a dot) of the
	 * domain name must be at most 63 characters long. This is verified by {@link IDN#toASCII(String)}.
	 */
	private static final int MAX_DOMAIN_PART_LENGTH = 255;

	/**
	 * Regular expression for the local part of an email address (everything before '@')
	 */
	private static final Pattern LOCAL_PART_PATTERN = Pattern.compile(
			LOCAL_PART_ATOM + "+(\\." + LOCAL_PART_ATOM + "+)*", CASE_INSENSITIVE
	);

	/**
	 * Regular expression for the domain part of an email address (everything after '@')
	 */
	private static final Pattern DOMAIN_PATTERN = Pattern.compile(
			DOMAIN + "|" + IP_DOMAIN, CASE_INSENSITIVE
	);

	@Override
	public void initialize(Email annotation) {
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}

		// split email at '@' and consider local and domain part separately;
		// note a split limit of 3 is used as it causes all characters following to an (illegal) second @ character to
		// be put into a separate array element, avoiding the regex application in this case since the resulting array
		// has more than 2 elements
		String[] emailParts = value.toString().split( "@", 3 );
		if ( emailParts.length != 2 ) {
			return false;
		}

		if ( !matchLocalPart( emailParts[0] ) ) {
			return false;
		}

		return matchDomain( emailParts[1] );
	}

	private boolean matchLocalPart(String localPart) {
		if ( localPart.length() > MAX_LOCAL_PART_LENGTH ) {
			return false;
		}
		Matcher matcher = LOCAL_PART_PATTERN.matcher( localPart );
		return matcher.matches();
	}

	private boolean matchDomain(String domain) {
		// if we have a trailing dot the domain part we have an invalid email address.
		// the regular expression match would take care of this, but IDN.toASCII drops the trailing '.'
		if ( domain.endsWith( "." ) ) {
			return false;
		}

		String asciiString;
		try {
			asciiString = IDN.toASCII( domain );
		}
		catch (IllegalArgumentException e) {
			return false;
		}

		if ( asciiString.length() > MAX_DOMAIN_PART_LENGTH ) {
			return false;
		}

		Matcher matcher = DOMAIN_PATTERN.matcher( asciiString );
		return matcher.matches();
	}

}
