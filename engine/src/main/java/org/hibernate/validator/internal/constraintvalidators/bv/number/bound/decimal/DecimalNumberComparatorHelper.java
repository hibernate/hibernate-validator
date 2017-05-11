/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.InfinityNumberComparatorHelper;

/**
 * @author Marko Bekhta
 */
public final class DecimalNumberComparatorHelper {

	private DecimalNumberComparatorHelper() {
	}

	public static int compare(BigDecimal number, BigDecimal value) {
		return number.compareTo( value );
	}

	public static int compare(BigInteger number, BigDecimal value) {
		return new BigDecimal( number ).compareTo( value );
	}

	public static int compare(Long number, BigDecimal value) {
		return BigDecimal.valueOf( number ).compareTo( value );
	}

	public static int compare(Number number, BigDecimal value) {
		return BigDecimal.valueOf( number.doubleValue() ).compareTo( value );
	}

	public static int compare(Double number, BigDecimal value, int treatNanAs) {
		Optional<Integer> infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.get();
		}
		return BigDecimal.valueOf( number ).compareTo( value );
	}

	public static int compare(Float number, BigDecimal value, int treatNanAs) {
		Optional<Integer> infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.get();
		}
		return BigDecimal.valueOf( number ).compareTo( value );
	}

}
