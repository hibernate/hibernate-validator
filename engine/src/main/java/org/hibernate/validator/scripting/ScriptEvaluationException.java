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
