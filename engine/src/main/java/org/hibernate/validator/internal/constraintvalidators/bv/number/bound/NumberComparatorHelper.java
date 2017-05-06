/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * @author Marko Bekhta
 */
public final class NumberComparatorHelper {

	private NumberComparatorHelper() {
	}

	public static int compare(BigDecimal number, long value) {
		return number.compareTo( BigDecimal.valueOf( value ) );
	}

	public static int compare(BigInteger number, long value) {
		return number.compareTo( BigInteger.valueOf( value ) );
	}

	public static int compare(Long number, long value) {
		return number.compareTo( value );
	}

	public static int compare(Number number, long value) {
		return Long.compare( number.longValue(), value );
	}

	public static int compare(Double number, long value, int treatNanAs) {
		Optional<Integer> infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.get();
		}
		return Long.compare( number.longValue(), value );
	}

	public static int compare(Float number, long value, int treatNanAs) {
		Optional<Integer> infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.get();
		}
		return Long.compare( number.longValue(), value );
	}

}
