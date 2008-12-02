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

import java.util.Set;

/**
 * Describe a constraint violation. This object describe the error context as
 * well as the message describing the violation.
 *
 * @author Emmanuel Bernard
 * @todo the rational behind rootBean and propertyPath is to keep the context available to the user
 */
public interface ConstraintViolation<T> {

	/**
	 * @return The interpolated error message for this constraint violation.
	 */
	String getInterpolatedMessage();

	/**
	 * @return The non-interpolated error message for this constraint violation.
	 */
	String getRawMessage();

	/**
	 * @return The root bean being validated.
	 */
	T getRootBean();

	/**
	 * If a bean constraint, the bean instance the constraint is applied on
	 * If a property constraint, the bean instance hosting the property the
	 * constraint is applied on
	 * 
	 * @return the leaf bean the constraint is applied on or null if Validator#validateValue
	 * is used
	 */
	Object getLeafBean();

	/**
	 * @return the property path to the value from <code>rootBean</code>
	 *         <code>null</code> if the value is the <code>rootBean<code> itself.
	 */
	String getPropertyPath();

	/**
	 * @return the value failing to pass the constraint.
	 */
	Object getInvalidValue();

	/**
	 * @return the list of groups that the triggered constraint applies on and which also are
	 *         within the list of groups requested for validation.
	 *
	 * TODO: considering removal, if you think it's important, speak up
	 */
	Set<String> getGroups();

	/**
	 * Constraint metadata reported to fail.
	 * The returned instance is immutable.
	 * 
	 * @return constraint metadata
	 */
	ConstraintDescriptor getConstraintDescriptor();
}
