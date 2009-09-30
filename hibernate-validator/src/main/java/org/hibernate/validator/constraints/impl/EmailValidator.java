//$Id$
package org.hibernate.validator.constraints.impl;

import java.util.regex.Matcher;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Email;

/**
 * Check that a given string is a well-formed email address.
 *
 * @author Emmanuel Bernard
 */
public class EmailValidator implements ConstraintValidator<Email, String> {
	//TODO: Implement this http://www.ex-parrot.com/~pdw/Mail-RFC822-Address.html regex in java
	private static String ATOM = "[^\\x00-\\x1F^\\(^\\)^\\<^\\>^\\@^\\,^\\;^\\:^\\\\^\\\"^\\.^\\[^\\]^\\s]";
	private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
	private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

	private java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
			"^" + ATOM + "+(\\." + ATOM + "+)*@"
					+ DOMAIN
					+ "|"
					+ IP_DOMAIN
					+ ")$",
			java.util.regex.Pattern.CASE_INSENSITIVE
	);

	public void initialize(Email annotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}
		Matcher m = pattern.matcher( value );
		return m.matches();
	}
}