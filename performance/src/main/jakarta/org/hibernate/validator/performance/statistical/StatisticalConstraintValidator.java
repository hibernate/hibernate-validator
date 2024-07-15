/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.statistical;

import java.lang.annotation.Annotation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class StatisticalConstraintValidator implements ConstraintValidator<Annotation, Object> {
	private static final float FAILURE_RATE = 0.25f;

	public static final ThreadLocal<Counter> threadLocalCounter = new ThreadLocal<Counter>() {
		@Override
		protected Counter initialValue() {
			return new Counter();
		}
	};

	@Override
	public void initialize(Annotation constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return threadLocalCounter.get().incrementCount();
	}

	public static class Counter {
		private int totalCount = 0;
		private int failures = 0;

		public int getFailures() {
			return failures;
		}

		public boolean incrementCount() {
			totalCount++;
			if ( totalCount * FAILURE_RATE > failures ) {
				failures++;
				return false;
			}
			return true;
		}

		public void reset() {
			totalCount = 0;
			failures = 0;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append( "Counter" );
			sb.append( "{totalCount=" ).append( totalCount );
			sb.append( ", failures=" ).append( failures );
			sb.append( '}' );
			return sb.toString();
		}
	}
}
