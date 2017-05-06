/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

import java.util.Optional;

/**
 * @author Marko Bekhta
 */
public final class InfinityNumberComparatorHelper {

	private InfinityNumberComparatorHelper() {
	}

	public static Optional<Integer> infinityCheck(Double number, int treatNanAs) {
		Integer result = null;
		if ( number == Double.NEGATIVE_INFINITY ) {
			result = -1;
		}
		else if ( number.isNaN() ) {
			result = treatNanAs;
		}
		else if ( number == Double.POSITIVE_INFINITY ) {
			result = 1;
		}
		return Optional.ofNullable( result );
	}

	public static Optional<Integer> infinityCheck(Float number, int treatNanAs) {
		Integer result = null;
		if ( number == Float.NEGATIVE_INFINITY ) {
			result = -1;
		}
		else if ( number.isNaN() ) {
			result = treatNanAs;
		}
		else if ( number == Float.POSITIVE_INFINITY ) {
			result = 1;
		}
		return Optional.ofNullable( result );
	}

}
