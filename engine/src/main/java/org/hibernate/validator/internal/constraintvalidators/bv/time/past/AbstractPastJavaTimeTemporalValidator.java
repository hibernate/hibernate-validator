/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.temporal.Temporal;

/**
 * @author Marko Bekhta
 */
public abstract class AbstractPastJavaTimeTemporalValidator<T extends Temporal & Comparable<? super T>> extends AbstractPastJavaTimeValidator<T> {

	@Override
	protected T adjustedReferenceValue(T value) {
		return (T) value.plus( tolerance );
	}
}
