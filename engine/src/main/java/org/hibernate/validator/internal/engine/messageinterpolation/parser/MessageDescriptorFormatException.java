/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
