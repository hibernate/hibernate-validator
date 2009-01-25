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

/**
 * Factory returning initialized Validator instances.
 * Implementations are thread-safe
 * This object is typically cached and reused.
 *
 * @author Emmanuel Bernard
 */
public interface ValidatorFactory {
	/**
	 * return an initialized Validator instance using the default factory instances
	 * for message interpolator and traversable resolver.
	 *
	 * Validator instances can be pooled and shared by the implementation
	 */
	Validator getValidator();

	/**
	 * Define the validator state and return a
	 * Validator compliant with this state
	 *
	 * @return a ValidatorBuilder
	 */
	ValidatorBuilder defineValidatorState();

	/**
	 * Returns the MessageInterpolator instance configured at initialization time
	 * for the ValidatorFactory
	 * This is the instance used by #getValidator(Class)
	 *
	 * @return MessageInterpolator instance
	 */
	MessageInterpolator getMessageInterpolator();
}
