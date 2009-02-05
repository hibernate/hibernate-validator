// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.validation;

import javax.validation.ValidationException;

/**
 * Exception raised in the case that the constraint validator resolution returns more than one possible validators for
 * a given type.
 *
 * @author Hardy Ferentschik
 */
public class UnexpectedTypeForConstraintException extends ValidationException {
	public UnexpectedTypeForConstraintException(String message) {
		super( message );
	}

	public UnexpectedTypeForConstraintException() {
		super();
	}

	public UnexpectedTypeForConstraintException(String message, Throwable cause) {
		super( message, cause );
	}

	public UnexpectedTypeForConstraintException(Throwable cause) {
		super( cause );
	}
}