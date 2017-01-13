/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.net.IDN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author Marko Bekhta
 */
public final class DomainNameUtil {

	/**
	 * This is the maximum length of a domain name. But be aware that each label (parts separated by a dot) of the
	 * domain name must be at most 63 characters long. This is verified by {@link IDN#toASCII(String)}.
	 */
	private static final int MAX_DOMAIN_PART_LENGTH = 255;

	private static final String DOMAIN_LABEL = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
	private static final String DOMAIN = DOMAIN_LABEL + "+(\\." + DOMAIN_LABEL + "+)*";
	private static final String IP_DOMAIN = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
	//IP v6 regex taken from http://stackoverflow.com/questions/53497/regular-expression-that-matches-valid-ipv6-addresses
	private static final String IP_V6_DOMAIN = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";

	/**
	 * Regular expression for the domain part of an email address (everything after '@')
	 */
	private static final Pattern DOMAIN_PATTERN = Pattern.compile(
			DOMAIN + "|" + IP_DOMAIN + "|" + "\\[IPv6:" + IP_V6_DOMAIN + "\\]", CASE_INSENSITIVE
	);

	private DomainNameUtil() {
	}

	/**
	 * Checks validity of domain name.
	 *
	 * @param domain domain to check for validity
	 *
	 * @return {@code true} if provided string is a valid domain, {@code false} otherwise
	 */
	public static boolean isValidDomainAddress(String domain) {
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
