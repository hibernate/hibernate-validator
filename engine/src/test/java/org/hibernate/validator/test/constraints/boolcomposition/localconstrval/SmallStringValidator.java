package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Test mode for HV-390.
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class SmallStringValidator implements ConstraintValidator<SmallString, String> {
	public void initialize(SmallString constraintAnnotation) {
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
