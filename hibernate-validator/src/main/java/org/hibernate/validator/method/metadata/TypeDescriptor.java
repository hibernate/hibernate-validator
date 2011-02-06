/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.method.metadata;

import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.PropertyDescriptor;

/**
 * Describes a constrained Java type and the constraints associated to it.
 *
 * @author Gunnar Morling
 */
public interface TypeDescriptor extends ElementDescriptor {

	/**
	 * Whether this type has any class-, property- or method-level constraints
	 * or not. This is <code>true</code>, if
	 * {@link BeanDescriptor#isBeanConstrained()} would return true for the same
	 * type. Furthermore this will be true, if
	 * <ul>
	 * <li>a constraint is hosted on any of this type's method's parameters</li>
	 * <li>any of this type's method's parameters is marked for cascaded
	 * validation with the {@link Valid} annotation</li>
	 * <li>a constraint is hosted on the return value of any of this type's
	 * methods</li>
	 * <li>the return value of any of this type's methods is marked for cascaded
	 * validation with the {@link Valid} annotation</li>
	 * </ul>
	 *
	 * @return <code>True</code>, if this type has any constraints,
	 *         <code>false</code>
	 */
	boolean isTypeConstrained();

	/**
	 * @see BeanDescriptor#getConstrainedProperties()
	 */
	Set<PropertyDescriptor> getConstrainedProperties();

	/**
	 * @see BeanDescriptor#getConstraintsForProperty(String)
	 */
	PropertyDescriptor getConstraintsForProperty(String propertyName);

	/**
	 * Returns a set with the constrained methods of this type.
	 *
	 * @return A set with the constrained methods of this type, will be empty if
	 *         none of this type's methods are constrained.
	 */
	Set<MethodDescriptor> getConstrainedMethods();

	/**
	 * Returns a descriptor for the specified method.
	 *
	 * @param method The method of interest.
	 *
	 * @return A descriptor for the specified method. Will never be null.
	 *
	 * @throws IllegalArgumentException if method is null or is not a method of the type represented
	 * by this descriptor.
	 */
	MethodDescriptor getConstraintsForMethod(Method method);

}
