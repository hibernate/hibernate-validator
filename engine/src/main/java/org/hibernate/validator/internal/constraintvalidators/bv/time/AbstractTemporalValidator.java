/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.Temporal;

/**
 * Base class for all {@code @Future} validators that are based on the {@code java.time} package.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public abstract class AbstractTemporalValidator<C extends Annotation, T extends Temporal> extends AbstractJavaTimeValidator<C, T> {

	@Override
	protected Duration getDifference(Clock reference, T value) {
		return Duration.between( value, adjustCurrentTime( getReferenceValue( reference ) ) );
	}

	protected abstract T getReferenceValue(Clock reference);

}
