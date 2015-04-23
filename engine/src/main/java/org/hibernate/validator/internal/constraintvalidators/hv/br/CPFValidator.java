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
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;

/**
 * @author Hardy Ferentschik
 */
public class CPFValidator implements ConstraintValidator<CPF, CharSequence> {
	private static final Pattern DIGITS_ONLY = Pattern.compile( "\\d+" );
	private static final Pattern SINGLE_DASH_SEPARATOR = Pattern.compile( "\\d+-\\d\\d" );

	private final Mod11CheckValidator withSeparatorMod11Validator1 = new Mod11CheckValidator();
	private final Mod11CheckValidator withSeparatorMod11Validator2 = new Mod11CheckValidator();

	private final Mod11CheckValidator withDashOnlySeparatorMod11Validator1 = new Mod11CheckValidator();
	private final Mod11CheckValidator withDashOnlySeparatorMod11Validator2 = new Mod11CheckValidator();

	private final Mod11CheckValidator withoutSeparatorMod11Validator1 = new Mod11CheckValidator();
	private final Mod11CheckValidator withoutSeparatorMod11Validator2 = new Mod11CheckValidator();

	@Override
	public void initialize(CPF constraintAnnotation) {
		// validates CPF strings with separator, eg 134.241.313-00
		// there are two checksums generated. The first over the digits prior the hyphen with the first
		// check digit being the digit directly after the hyphen. The second checksum is over all digits
		// pre hyphen + first check digit. The check digit in this case is the second digit after the hyphen
		withSeparatorMod11Validator1.initialize(
				0, 10, 12, true, Integer.MAX_VALUE, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
		withSeparatorMod11Validator2.initialize(
				0, 12, 13, true, Integer.MAX_VALUE, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);

		// validates CPF strings with separator, eg 134241313-00
		withDashOnlySeparatorMod11Validator1.initialize(
				0, 8, 10, true, Integer.MAX_VALUE, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
		withDashOnlySeparatorMod11Validator2.initialize(
				0, 10, 11, true, Integer.MAX_VALUE, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);

		// validates CPF strings without separator, eg 13424131300
		// checksums as described above, except there are no separator characters
		withoutSeparatorMod11Validator1.initialize(
				0, 8, 9, true, Integer.MAX_VALUE, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
		withoutSeparatorMod11Validator2.initialize(
				0, 9, 10, true, Integer.MAX_VALUE, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
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
		else if ( SINGLE_DASH_SEPARATOR.matcher( value ).matches() ) {
			return withDashOnlySeparatorMod11Validator1.isValid( value, context )
					&& withDashOnlySeparatorMod11Validator2.isValid( value, context );
		}
		else {
			return withSeparatorMod11Validator1.isValid( value, context )
					&& withSeparatorMod11Validator2.isValid( value, context );

		}
	}
}
