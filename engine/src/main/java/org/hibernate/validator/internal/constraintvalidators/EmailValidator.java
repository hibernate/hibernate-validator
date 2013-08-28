/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.constraintvalidators;

import java.net.IDN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Email;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

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
 */
public class EmailValidator implements ConstraintValidator<Email, CharSequence> {
	private static String ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
	private static String DOMAIN = ATOM + "+(\\." + ATOM + "+)*";
	private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

	/**
	 * Regular expression for the local part of an email address (everything before '@')
	 */
	private final Pattern localPattern = java.util.regex.Pattern.compile(
			ATOM + "+(\\." + ATOM + "+)*", CASE_INSENSITIVE
	);

	/**
	 * Regular expression for the domain part of an email address (everything after '@')
	 */
	private final Pattern domainPattern = java.util.regex.Pattern.compile(
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

		// if we have a trailing dot in local or domain part we have an invalid email address.
		// the regular expression match would take care of this, but IDN.toASCII drops trailing the trailing '.'
		// (imo a bug in the implementation)
		if ( emailParts[0].endsWith( "." ) || emailParts[1].endsWith( "." ) ) {
			return false;
		}

		if ( !matchPart( emailParts[0], localPattern ) ) {
			return false;
		}

		return matchPart( emailParts[1], domainPattern );
	}

	private boolean matchPart(String part, Pattern pattern) {
		try {
			part = IDN.toASCII( part );
		}
		catch ( IllegalArgumentException e ) {
			// occurs when the label is too long (>63, even though it should probably be 64 - see http://www.rfc-editor.org/errata_search.php?rfc=3696,
			// practically that should not be a problem)
			return false;
		}
		Matcher matcher = pattern.matcher( part );
		return matcher.matches();
	}
}
