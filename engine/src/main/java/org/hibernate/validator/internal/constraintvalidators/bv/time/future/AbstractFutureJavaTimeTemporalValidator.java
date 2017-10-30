/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.temporal.Temporal;

/**
 * @author Marko Bekhta
 */
public abstract class AbstractFutureJavaTimeTemporalValidator<T extends Temporal & Comparable<? super T>> extends AbstractFutureJavaTimeValidator<T> {

	@Override
	protected T adjustedReferenceValue(T value) {
		return (T) value.minus( tolerance );
	}
}
