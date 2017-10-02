/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.scripting;

import javax.validation.ValidationException;

/**
 * @author Marko Bekhta
 */
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
