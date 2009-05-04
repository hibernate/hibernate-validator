package org.hibernate.validation.constraints.custom;

/**
 * @author Emmanuel Bernard
 */
public class PositiveConstraintValidator extends BoundariesConstraintValidator<Positive> {
	public void initialize(Positive constraintAnnotation) {
		super.initialize( 0, Integer.MAX_VALUE );
	}
}
