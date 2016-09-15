//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.crossparameter;

//end::include[]

import java.util.Date;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

//tag::include[]
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class ConsistentDateParametersValidator implements
		ConstraintValidator<ConsistentDateParameters, Object[]> {

	@Override
	public void initialize(ConsistentDateParameters constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object[] value, ConstraintValidatorContext context) {
		if ( value.length != 2 ) {
			throw new IllegalArgumentException( "Illegal method signature" );
		}

		//leave null-checking to @NotNull on individual parameters
		if ( value[0] == null || value[1] == null ) {
			return true;
		}

		if ( !( value[0] instanceof Date ) || !( value[1] instanceof Date ) ) {
			throw new IllegalArgumentException(
					"Illegal method signature, expected two " +
							"parameters of type Date."
			);
		}

		return ( (Date) value[0] ).before( (Date) value[1] );
	}
}
//end::include[]
