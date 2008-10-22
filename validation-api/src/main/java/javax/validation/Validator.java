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

import java.io.Serializable;
import java.util.Set;

/**
 * Validate a given object type.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Should Serializable be part of the definition?
 */
public interface Validator<T> extends Serializable {
	/**
	 * validate all constraints on object (unless shortcut)
	 *
	 * @param object object to validate
	 * @param groups group name(s) targeted for validation (default to <code>default</code>
	 *
	 * @return array of invalid constrains or an empty array if none.
	 *
	 * @throws IllegalArgumentException e if object is <code>null</code>.
	 */
	Set<InvalidConstraint<T>> validate(T object, String... groups);

	/**
	 * validate all constraints on propertyname property of object (unless shortcut)
	 * <p/>
	 *
	 * @param object object to validate
	 * @param propertyName property to validate
	 * @param groups group name(s) targeted for validation (default to <code>default</code>
	 *
	 * @return array of invalid constrains or an empty array if none
	 *
	 * @throws IllegalArgumentException e if object is  <code>null</code>.
	 * @todo Do we keep this method?
	 */
	Set<InvalidConstraint<T>> validateProperty(T object, String propertyName, String... groups);

	/**
	 * Validates all constraints on propertyname property if the property value is value (unless shortcut)
	 * <p/>
	 *
	 * @param propertyName property to validate
	 * @param value property value to validate
	 * @param groups group name(s) targeted for validation (default to <code>default</code>
	 *
	 * @return array of invalid constrains or an empty array if none
	 *
	 * @todo Do we keep this method?
	 * @todo express limitations of InvalidConstraint in this case.
	 */
	Set<InvalidConstraint<T>> validateValue(String propertyName, Object value, String... groups);

	/**
	 * return true if at least one constraint declaration is present for the given bean
	 * or if one property is marked for validation cascade
	 */
	boolean hasConstraints();

	/**
	 * return the class level constraints
	 */
	ElementDescriptor getConstraintsForBean();

	/**
	 * return the property level constraints for a given propertyName
	 * or null if either the property does not exist or has no constraint
	 */
	ElementDescriptor getConstraintsForProperty(String propertyName);	

	/**
	 * return the property names having at least a constraint defined
	 */
	String[] getValidatedProperties();

}
