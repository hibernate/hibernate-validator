/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.performance.statistical;

import java.lang.annotation.Annotation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class StatisticalConstraintValidator implements ConstraintValidator<Annotation, Object> {
	private static final float FAILURE_RATE = 0.25f;

	public static final ThreadLocal<Counter> threadLocalCounter = new ThreadLocal<Counter>() {
		protected Counter initialValue() {
			return new Counter();
		}
	};

	public void initialize(Annotation constraintAnnotation) {
	}

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


