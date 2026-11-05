package org.hibernate.validator.test.internal.util;

/**
 * @author Emmanuel Bernard
 */
public class PositiveConstraintValidator extends BoundariesConstraintValidator<Positive> {
	@Override
	public void initialize(Positive constraintAnnotation) {
		super.initialize( 0, Integer.MAX_VALUE );
	}
}
