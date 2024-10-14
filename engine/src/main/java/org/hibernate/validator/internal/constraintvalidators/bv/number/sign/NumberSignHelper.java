/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalInt;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;

/**
 * @author Marko Bekhta
 */
final class NumberSignHelper {

	private NumberSignHelper() {
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

	static int signum(Float number, OptionalInt treatNanAs) {
		OptionalInt infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.getAsInt();
		}
		return number.compareTo( 0F );
	}

	static int signum(Double number, OptionalInt treatNanAs) {
		OptionalInt infinity = InfinityNumberComparatorHelper.infinityCheck( number, treatNanAs );
		if ( infinity.isPresent() ) {
			return infinity.getAsInt();
		}
		return number.compareTo( 0D );
	}
}
