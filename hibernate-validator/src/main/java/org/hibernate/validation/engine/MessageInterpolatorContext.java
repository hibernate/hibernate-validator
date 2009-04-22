package org.hibernate.validation.engine;

import javax.validation.ConstraintDescriptor;
import javax.validation.MessageInterpolator;

/**
 * Takes mandatory elements in the constructor
 *
 * @author Emmanuel Bernard
 */
public class MessageInterpolatorContext implements MessageInterpolator.Context {
	private final ConstraintDescriptor<?> constraintDescriptor;
	private final Object validatedValue;

	public MessageInterpolatorContext(ConstraintDescriptor<?> constraintDescriptor, Object validatedValue) {
		this.constraintDescriptor = constraintDescriptor;
		this.validatedValue = validatedValue;
	}

	public ConstraintDescriptor<?> getConstraintDescriptor() {
		return constraintDescriptor;
	}

	public Object getValidatedValue() {
		return validatedValue;
	}
}
