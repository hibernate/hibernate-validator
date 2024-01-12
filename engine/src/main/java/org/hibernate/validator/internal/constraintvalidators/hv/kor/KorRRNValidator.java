
/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.kor;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import org.hibernate.validator.constraints.kor.KorRRN;

public class KorRRNValidator implements ConstraintValidator<KorRRN, CharSequence> {

	private static final Pattern KOR_RRN_REGEX = Pattern.compile(
			"\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])-[1234]\\d{6}$" );

	@Override
	public boolean isValid(CharSequence rrnValue, ConstraintValidatorContext context) {
		if ( rrnValue == null ) {
			return true;
		}
		return KOR_RRN_REGEX.matcher( rrnValue.toString() ).matches();
	}
}
