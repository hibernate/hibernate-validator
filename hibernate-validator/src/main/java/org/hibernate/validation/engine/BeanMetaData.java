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
package org.hibernate.validation.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import javax.validation.BeanDescriptor;
import javax.validation.PropertyDescriptor;

/**
 * Interface defining the meta data about the constraints defined in a given bean.
 *
 * @author Hardy Ferentschik
 */
public interface BeanMetaData<T> {

	/**
	 * @return the class of the bean.
	 */
	Class<T> getBeanClass();

	/**
	 * @return an instance of <code>ElementDescriptor</code> describing the bean this meta data applies for.
	 */
	BeanDescriptor getBeanDescriptor();

	/**
	 * @return A list of all cascaded fields (fields annotated with &#064;Valid).
	 */
	List<Field> getCascadedFields();

	/**
	 * @return A list of all cascaded methods (methods annotated with &#064;Valid).
	 */
	List<Method> getCascadedMethods();

	/**
	 * @return A list of all cascaded methods and fields (methods/fields annotated with &#064;Valid).
	 */
	List<Member> getCascadedMembers();

	/**
	 * @return A map mapping defined group sequences to a list of groups.
	 */
	List<Class<?>> getDefaultGroupSequence();

	/**
	 * @return A list of <code>MetaConstraint</code> instances encapsulating the information of all the constraints
	 *         defined on the bean.
	 */
	List<MetaConstraint<T, ?>> geMetaConstraintList();

	/**
	 * Return <code>PropertyDescriptor</code> for the given property.
	 *
	 * @param property the property for which to retrieve the descriptor.
	 *
	 * @return Returns the <code>PropertyDescriptor</code> for the given property or <code>null</code> in case the
	 *         property does not have a descriptor.
	 */
	PropertyDescriptor getPropertyDescriptor(String property);

	/**
	 * @return the property names having at least one constraint defined or which are marked
	 *         as cascaded (@Valid).
	 */
	Set<String> getConstrainedProperties();
}
