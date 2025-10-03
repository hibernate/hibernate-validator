/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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

/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/IP_address">IP address</a>.
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 *
 * @author Ivan Malutin
 * @since 9.1
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface IpAddress {

	String message() default "{org.hibernate.validator.constraints.IpAddress.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	Type type() default Type.IPv4;

	/**
	 * Defines the IP address version.
	 * Valid IP address versions are:
	 * <ul>
	 *   <li>{@code IPv4} - for IPv4 addresses (version 4)</li>
	 *   <li>{@code IPv6} - for IPv6 addresses (version 6)</li>
	 *   <li>{@code ANY} - for validating IP addresses that could be either IPv4 or IPv6</li>
	 * </ul>
	 * When using {@code ANY}, an address is considered valid if it passes either
	 * IPv4 or IPv6 validation.
	 */
	enum Type {
		IPv4,
		IPv6,
		ANY
	}
}
