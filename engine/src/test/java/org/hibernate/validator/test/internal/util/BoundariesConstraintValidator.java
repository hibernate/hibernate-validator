/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
		return value >= low && value <= high;
	}
}
