/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import jakarta.validation.ValidationException;

/**
 * Exception thrown in case the message descriptor is invalid, for example unbalanced braces or escape characters
 *
 * @author Hardy Ferentschik
 */
public class MessageDescriptorFormatException extends ValidationException {
	public MessageDescriptorFormatException(String s) {
		super( s );
	}
}
