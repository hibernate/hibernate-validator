/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

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
	 * @return an instance of {@code ElementDescriptor} describing the bean this meta data applies for.
	 */
	BeanDescriptor getBeanDescriptor();

	/**
	 * @return A list of all cascaded methods and fields (methods/fields annotated with &#064;Valid).
	 */
	Set<Member> getCascadedMembers();

	/**
	 * Get the composition of the default group sequence.
	 * <p>
	 * If the bean state is given in parameter and the bean metadata has a default group sequence provider then the
	 * dynamic default group sequence composition is returned. In the other cases the default group sequence
	 * redefinition specified by BV is used.
	 * </p>
	 *
	 * @param beanState the bean state.
	 *
	 * @return a list of classes representing the default group sequence.
	 */
	List<Class<?>> getDefaultGroupSequence(T beanState);

	/**
	 * @return {@code true} if the entity redefines the default group sequence, {@code false} otherwise.
	 */
	boolean defaultGroupSequenceIsRedefined();

	/**
	 * @return A map of {@code MetaConstraint} instances encapsulating the information of all the constraints
	 *         defined on the bean mapped to the class in which the constraints is defined.
	 */
	Map<Class<?>, List<BeanMetaConstraint<T, ? extends Annotation>>> getMetaConstraintsAsMap();

	/**
	 * @return A list of {@code MetaConstraint} instances encapsulating the information of all the constraints
	 *         defined on the bean.
	 */
	List<BeanMetaConstraint<T, ? extends Annotation>> getMetaConstraintsAsList();

	/**
	 * Returns the constraint-related meta data for the given method of the
	 * class represented by this bean meta data.
	 *
	 * @param method The method of interest.
	 *
	 * @return A map with constraint-related meta data, keyed by the class
	 *         defining it (will only contain more than one entry, if the given
	 *         method is overwritten from an interface or base class. In this
	 *         case an entry for each class/interface in the inheritance
	 *         hierarchy is returned).
	 */
	Map<Class<?>, MethodMetaData> getMetaDataForMethod(Method method);

	/**
	 * Return {@code PropertyDescriptor} for the given property.
	 *
	 * @param property the property for which to retrieve the descriptor.
	 *
	 * @return Returns the {@code PropertyDescriptor} for the given property or {@code null} in case the
	 *         property does not have a descriptor.
	 */
	PropertyDescriptor getPropertyDescriptor(String property);

	/**
	 * @param name The name of the property
	 *
	 * @return true if the property exists on the object
	 *         even if the property does not host any constraint nor is cascaded
	 */
	boolean isPropertyPresent(String name);

	/**
	 * @return the property descriptors having at least one constraint defined or which are marked
	 *         as cascaded (@Valid).
	 */
	Set<PropertyDescriptor> getConstrainedProperties();

	/**
	 * <p>Checks the method parameter constraints of the type represented by this
	 * meta data object for correctness.
	 * </p>
	 * <p>
	 * The following rules apply for this check:
	 * </p>
	 * <ul>
	 * <li>Only the root method of an overridden method in an inheritance
	 * hierarchy may be annotated with parameter constraints in order to avoid
	 * the strengthening of a method's preconditions by additional parameter
	 * constraints defined at sub-types. If the root method itself has no
	 * parameter constraints, also no parameter constraints may be added in
	 * sub-types.</li>
	 * <li>If there are multiple root methods for an method in an inheritance
	 * hierarchy (e.g. by implementing two interfaces defining the same method)
	 * no parameter constraints for this method are allowed at all in order to
	 * avoid a strengthening of a method's preconditions in parallel types.</li>
	 * </ul>
	 *
	 * @throws ConstraintDeclarationException In case the represented bean
	 * has an illegal method parameter constraint.
	 */
	void assertMethodParameterConstraintsCorrectness() throws ConstraintDeclarationException;
}
