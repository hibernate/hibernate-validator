/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.scripting;

import jakarta.validation.ValidationException;

import org.hibernate.validator.Incubating;

/**
 * Exception raised when a script evaluator cannot be found for a given language.
 *
 * @author Marko Bekhta
 * @since 6.0.3
 */
@Incubating
public class ScriptEvaluatorNotFoundException extends ValidationException {

	public ScriptEvaluatorNotFoundException() {
	}

	public ScriptEvaluatorNotFoundException(String message) {
		super( message );
	}

	public ScriptEvaluatorNotFoundException(Throwable cause) {
		super( cause );
	}

	public ScriptEvaluatorNotFoundException(String message, Throwable cause) {
		super( message, cause );
	}
}
