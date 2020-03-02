/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
