// $Id: AmbiguousConstraintUsageException.java 15902 2009-02-05 11:53:49Z hardy.ferentschik $
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
package org.hibernate.validation;

import javax.validation.ValidationException;
import javax.validation.UnexpectedTypeException;

/**
 * Exception raised in the case that the constraint validator resolution returns more than one possible validators for
 * a given type.
 *
 * @author Hardy Ferentschik
 */
public class AmbiguousConstraintUsageException extends UnexpectedTypeException {
	public AmbiguousConstraintUsageException(String message) {
		super( message );
	}

	public AmbiguousConstraintUsageException() {
		super();
	}

	public AmbiguousConstraintUsageException(String message, Throwable cause) {
		super( message, cause );
	}

	public AmbiguousConstraintUsageException(Throwable cause) {
		super( cause );
	}
}
