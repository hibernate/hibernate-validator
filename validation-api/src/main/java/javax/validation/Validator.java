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
 * Validate bean instances
 * Implementations of this interface must be thread-safe
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Should Serializable be part of the definition?
 */
public interface Validator {
	/**
	 * validate all constraints on object
	 *
	 * @param object object to validate
	 * @param groups groups targeted for validation
	 *               (default to {@link javax.validation.groups.Default})
	 *
	 * @return constraint violations or an empty Set if none
	 *
	 * @throws IllegalArgumentException e if object is null
	 */
	<T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups);

	/**
	 * validate all constraints on &lt;code&gt;propertyName&lt;/code&gt; property of object
	 *
	 * @param object object to validate
	 * @param propertyName property to validate (ie field and getter constraints)
	 * @param groups groups targeted for validation
	 *               (default to {@link javax.validation.groups.Default})
	 *
	 * @return constraint violations or an empty Set if none
	 *
	 * @throws IllegalArgumentException e if object is null or if propertyName is not present
	 */
	<T> Set<ConstraintViolation<T>> validateProperty(T object,
													 String propertyName,
													 Class<?>... groups);

	/**
	 * validate all constraints on <code>propertyName</code> property
	 * if the property value is <code>value</code>
	 * <p/>
	 * TODO express limitations of ConstraintViolation in this case
	 *
	 * @param propertyName property to validate
	 * @param value property value to validate
	 * @param groups groups targeted for validation
	 *               (default to {@link javax.validation.groups.Default})
	 *
	 * @return constraint violations or an empty Set if none
	 * @throws IllegalArgumentException e if propertyName is not present
	 */
	<T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType,
												  String propertyName,
												  Object value,
												  Class<?>... groups);

	/**
	 * Return the descriptor object describing bean constraints
	 * The returned object (and associated objects including ConstraintDescriptors)
     * are immutable.
	 *
	 * @param clazz class type evaluated
	 */
	BeanDescriptor getConstraintsForClass(Class<?> clazz);
}
