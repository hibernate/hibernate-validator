/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Duration;
import java.time.temporal.TemporalAccessor;

import jakarta.validation.constraints.Past;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractJavaTimeValidator;

/**
 * Base class for all {@code @Past} validators that are based on the {@code java.time} package.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractPastJavaTimeValidator<T extends TemporalAccessor & Comparable<? super T>> extends AbstractJavaTimeValidator<Past, T> {

	@Override
	protected boolean isValid(int result) {
		return result < 0;
	}

	@Override
	protected Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance) {
		return absoluteTemporalValidationTolerance;
	}
}
