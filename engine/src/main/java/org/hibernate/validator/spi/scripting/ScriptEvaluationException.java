/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.scripting;

import jakarta.validation.ValidationException;

import org.hibernate.validator.Incubating;

/**
 * Exception raised when an error occurs during the evaluation of a script.
 *
 * @author Marko Bekhta
 * @since 6.0.3
 */
@Incubating
public class ScriptEvaluationException extends ValidationException {

	public ScriptEvaluationException() {
	}

	public ScriptEvaluationException(String message) {
		super( message );
	}

	public ScriptEvaluationException(Throwable cause) {
		super( cause );
	}

	public ScriptEvaluationException(String message, Throwable cause) {
		super( message, cause );
	}
}
