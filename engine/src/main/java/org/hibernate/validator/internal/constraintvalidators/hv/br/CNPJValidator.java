/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;

/**
 * @author Hardy Ferentschik
 */
public class CNPJValidator implements ConstraintValidator<CNPJ, CharSequence> {
	private static final Pattern DIGITS_ONLY = Pattern.compile( "\\d+" );

	private final Mod11CheckValidator withSeparatorMod11Validator1 = new Mod11CheckValidator();
	private final Mod11CheckValidator withSeparatorMod11Validator2 = new Mod11CheckValidator();

	private final Mod11CheckValidator withoutSeparatorMod11Validator1 = new Mod11CheckValidator();
	private final Mod11CheckValidator withoutSeparatorMod11Validator2 = new Mod11CheckValidator();

	@Override
	public void initialize(CNPJ constraintAnnotation) {
		// validates CNPJ strings with separator, eg 91.509.901/0001-69
		// there are two checksums generated. The first over the digits prior the hyphen with the first
		// check digit being the digit directly after the hyphen. The second checksum is over all digits
		// pre hyphen + first check digit. The check digit in this case is the second digit after the hyphen
		withSeparatorMod11Validator1.initialize(
				0, 14, 16, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
		withSeparatorMod11Validator2.initialize(
				0, 16, 17, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);

		// validates CNPJ strings without separator, eg 91509901000169
		// checksums as described above, except there are no separator characters
		withoutSeparatorMod11Validator1.initialize(
				0, 11, 12, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
		withoutSeparatorMod11Validator2.initialize(
				0, 12, 13, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		if ( DIGITS_ONLY.matcher( value ).matches() ) {
			return withoutSeparatorMod11Validator1.isValid( value, context )
					&& withoutSeparatorMod11Validator2.isValid( value, context );
		}
		else {
			return withSeparatorMod11Validator1.isValid( value, context )
					&& withSeparatorMod11Validator2.isValid( value, context );
		}
	}
}
