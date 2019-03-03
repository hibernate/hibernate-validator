/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalInt;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;

/**
 * @author Marko Bekhta
 */
final class NumberComparatorHelper {

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

	public static int compare(Number number, long value, OptionalInt treatNanAs) {
		// In case of comparing numbers we need to check for special cases:
		// 1. Floating point numbers should consider nan/infinity as values hence they should
		// be directed to corresponding overloaded methods:
		if ( number instanceof Double ) {
			return compare( (Double) number, value, treatNanAs );
		}
		if ( number instanceof Float ) {
			return compare( (Float) number, value, treatNanAs );
		}

		// 2. For big numbers we don't want to lose any data so we just cast them and call corresponding methods:
		if ( number instanceof BigDecimal ) {
			return compare( (BigDecimal) number, value );
		}
		if ( number instanceof BigInteger ) {
			return compare( (BigInteger) number, value );
		}

		// 3. For any integer types we convert them to long as we would do that anyway
		// when comparing with a long value. And use corresponding method for longs:
		if ( number instanceof Byte || number instanceof Integer || number instanceof Long || number instanceof Short ) {
			return compare( number.longValue(), value );
		}

		// 4. As a fallback we convert the number to double:
		return compare( number.doubleValue(), value, treatNanAs );
	}

	public static int compare(Double number, long value, OptionalInt treatNanAs) {
		OptionalInt infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.getAsInt();
		}
		return Double.compare( number, value );
	}

	public static int compare(Float number, long value, OptionalInt treatNanAs) {
		OptionalInt infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.getAsInt();
		}
		return Float.compare( number, value );
	}
}
