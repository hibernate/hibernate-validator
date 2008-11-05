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

import java.util.Locale;

/**
 * Interpolate a given constraint violation message.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface MessageResolver {
	/**
	 * Interpolate the message from the constraint parameters and the actual validated object.
	 * The locale is defaulted according to the <code>MessageResolver</code> implementation
	 * See the implementation documentation for more detail.
	 *
	 * @param message The message to interpolate.
	 * @param constraintDescriptor The constraint descriptor.
	 * @param value The object being validated
	 *
	 * @return Interpolated error message.
	 */
	String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value);

	/**
	 * Interpolate the message from the constraint parameters and the actual validated object.
	 * The Locale used is provided as a parameter
	 *
	 * @param message The message to interpolate.
	 * @param constraintDescriptor The constraint descriptor.
	 * @param value The object being validated
	 * @param locale the locale targeted for the message
	 *
	 * @return Interpolated error message.
	 */
	String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value, Locale locale);
}
