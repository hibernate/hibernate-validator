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
	 * validate all constraints on object
	 *
	 * @param object object to validate
	 * @param groups group name(s) (including group sequence names) targeted for validation (default to &lt;code&gt;default&lt;/code&gt;)
	 *
	 * @return constraint violations or an empty Set if none
	 *
	 * @throws IllegalArgumentException e if object is null
	 */
	Set<ConstraintViolation<T>> validate(T object, String... groups);

	/**
	 * validate all constraints on &lt;code&gt;propertyName&lt;/code&gt; property of object
	 *
	 * @param object object to validate
	 * @param propertyName property to validate (ie field and getter constraints)
	 * @param groups group name(s) (including group sequence names) targeted for validation (default to &lt;code&gt;default&lt;/code&gt;)
	 *
	 * @return constraint violations or an empty Set if none
	 *
	 * @throws IllegalArgumentException e if object is null
	 */
	Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, String... groups);

	/**
	 * validate all constraints on <code>propertyName</code> property
	 * if the property value is <code>value</code>
	 * <p/>
	 * TODO express limitations of ConstraintViolation in this case
	 *
	 * @param propertyName property to validate
	 * @param value property value to validate
	 * @param groups group name(s) (including group sequence names) targeted for validation (default to <code>default</code>)
	 *
	 * @return constraint violations or an empty Set if none
	 */
	Set<ConstraintViolation<T>> validateValue(String propertyName, Object value, String... groups);

	/**
	 * return true if at least one constraint declaration is present for the given bean
	 * or if one property is marked for validation cascade
	 */
	boolean hasConstraints();

	/**
	 * return the class level constraints
	 */
	BeanDescriptor getConstraintsForClass();

	/**
	 * return the property level constraints for a given propertyName
	 * or null if either the property does not exist or has no constraint
	 */
	PropertyDescriptor getConstraintsForProperty(String propertyName);

	/**
	 * return the property names having at least a constraint defined
	 */
	Set<String> getPropertiesWithConstraints();

}
