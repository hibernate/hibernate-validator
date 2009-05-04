package org.hibernate.validation.constraints.custom;

import java.lang.annotation.Annotation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Emmanuel Bernard
 */
public abstract class BoundariesConstraintValidator<T extends Annotation> implements ConstraintValidator<T, Integer> {
	private int low;
	private int high;

	protected void initialize(int low, int high) {
		this.low = low;
		this.high = high;
	}

	public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
		return value >= low && value <= high;
	}
}
