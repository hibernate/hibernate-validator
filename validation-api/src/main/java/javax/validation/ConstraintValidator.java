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

import java.lang.annotation.Annotation;

/**
 * Define the logic to validate a given constraint.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConstraintValidator<A extends Annotation> {
	/**
	 * Validator parameters for a given constraint definition
	 * Annotations parameters are passed as key/value into parameters
	 * <p/>
	 * This method is guaranteed to be called before any of the other Constraint
	 * implementation methods
	 *
	 * @param constraintAnnotation parameters for a given constraint definition
	 */
	void initialize(A constraintAnnotation);

	/**
	 * Implement the validation constraint.
	 * <code>object</code> state must not be changed by a Constraint implementation
	 *
	 * @param object object to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 *
	 * @return false if <code>object</code> does not pass the constraint
	 */
	boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext);
}
 