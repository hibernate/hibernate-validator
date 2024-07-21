/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.Incubating;

/**
 * The string has to be a well-formed BTC (Bitcoin) Mainnet address. Accepts {@code CharSequence}.
 * P2PK, P2MS and Nested SegWit (P2SH-P2WPKH and P2SH-P2WSH) addresses are not valid.
 * <p>
 * {@code null} elements are considered valid.
 *
 * @author José Yoshiriro
 *
 * @since 9.0.0
 */
@Incubating
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface BitcoinAddress {

	String message() default "{org.hibernate.validator.constraints.BitcoinAddress.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The type of the address to be considered as valid.
	 * @see BitcoinAddressType
	 */
	BitcoinAddressType[] value() default BitcoinAddressType.ANY;

	enum BitcoinAddressType {
		ANY, P2PKH, P2SH, BECH32, P2WSH, P2WPKH, P2TR;
	}
}
