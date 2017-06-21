/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future.present;

import java.time.temporal.TemporalAccessor;

import javax.validation.constraints.FutureOrPresent;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractJavaTimeValidator;

/**
 * Base class for all {@code @FutureOrPresent} validators that are based on the {@code java.time} package.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractFutureOrPresentJavaTimeValidator<T extends TemporalAccessor & Comparable<? super T>> extends AbstractJavaTimeValidator<FutureOrPresent, T> {

	@Override
	protected boolean isValid(int result) {
		return result >= 0;
	}

}
