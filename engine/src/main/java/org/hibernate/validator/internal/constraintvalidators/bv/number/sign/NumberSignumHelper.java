/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.InfinityNumberComparatorHelper;

/**
 * @author Marko Bekhta
 */
public final class NumberSignumHelper {

	private NumberSignumHelper() {
	}

	private static final short SHORT_ZERO = (short) 0;

	private static final byte BYTE_ZERO = (byte) 0;

	static int signum(Long number) {
		return Long.signum( number );
	}

	static int signum(Integer number) {
		return Integer.signum( number );
	}

	static int signum(Short number) {
		return number.compareTo( SHORT_ZERO );
	}

	static int signum(Byte number) {
		return number.compareTo( BYTE_ZERO );
	}

	static int signum(BigInteger number) {
		return number.signum();
	}

	static int signum(BigDecimal number) {
		return number.signum();
	}

	static int signum(Number value) {
		return Double.compare( value.doubleValue(), 0D );
	}

	static int signum(Float number, int treatNanAs) {
		Optional<Integer> infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.get();
		}
		return number.compareTo( 0F );
	}

	static int signum(Double number, int treatNanAs) {
		Optional<Integer> infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.get();
		}
		return number.compareTo( 0D );
	}
}
