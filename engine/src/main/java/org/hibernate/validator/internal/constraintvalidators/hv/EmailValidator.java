/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Email;
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
public class EmailValidator implements ConstraintValidator<Email, CharSequence> {

	@Override
	public void initialize(Email annotation) {
	}

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

		if ( !DomainNameUtil.isValidEmailLocalPart( localPart ) ) {
			return false;
		}

		return DomainNameUtil.isValidDomainAddress( domainPart );
	}


}
