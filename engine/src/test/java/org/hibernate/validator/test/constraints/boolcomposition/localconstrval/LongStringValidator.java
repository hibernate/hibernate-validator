package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Test mode for HV-390.
 *
 * Copy of SmallStringValidator, but in this case used to validate another annotation type.
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class LongStringValidator implements ConstraintValidator<PatternOrLong, String> {

	public void initialize(PatternOrLong constraintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		boolean pass;
		if ( value == null ) {
			pass = true;
		}
		else {
			pass = value.length() > 10;
		}
		return pass;
	}
}
