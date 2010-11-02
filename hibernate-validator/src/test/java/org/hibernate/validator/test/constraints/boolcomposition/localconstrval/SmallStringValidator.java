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

	@Override
	public void initialize(SmallString constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		boolean pass;
		if(value==null)
			pass=true;
		else
			pass=value.length()>10;
		return pass;
	}

}
