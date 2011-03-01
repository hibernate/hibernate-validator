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

import java.util.Set;
import javax.validation.Valid;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ElementDescriptor;

/**
 * Describes a constrained Java type and the constraints associated to it.
 *
 * @author Gunnar Morling
 */
public interface TypeDescriptor extends ElementDescriptor {

	/**
	 * Whether this type has any class-, property- (field or getter style) or
	 * method-level constraints or not. This is the case if
	 * <ul>
	 * <li>a constraint is hosted at this type itself,</li>
	 * <li>a constraint is hosted on any of this type's bean properties
	 * (represented by fields or getter methods),</li>
	 * <li>any of this type's bean properties (represented by fields or getter
	 * methods) is marked for cascaded validation with the {@link Valid}
	 * annotation,</li>
	 * <li>a constraint is hosted on any of this type's method's parameters,</li>
	 * <li>any of this type's method's parameters is marked for cascaded
	 * validation with the {@link Valid} annotation,</li>
	 * <li>a constraint is hosted on the return value of any of this type's
	 * methods or</li>
	 * <li>the return value of any of this type's methods is marked for cascaded
	 * validation with the {@link Valid} annotation</li>
	 * </ul>
	 *
	 * @return <code>True</code>, if this type has any constraints,
	 *         <code>false</code>
	 */
	boolean isTypeConstrained();

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
	 * @param methodName The name of the method to retrieve a descriptor for.
	 * @param parameterTypes The types of the parameters of the method to retrieve a
	 * descriptor for, in the order as they are defined in the method
	 * signature.
	 *
	 * @return A descriptor for the specified method or <code>null</null>, if no method
	 *         with the given name and parameter types is declared on the type represented
	 *         by this descriptor or its super types.
	 *
	 * @throws IllegalArgumentException if {@code methodName} is null.
	 */
	MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes);

	/**
	 * Returns a descriptor for the bean related constraints of this type.
	 *
	 * @return A bean descriptor. Will never be null, also if this type has no bean properties.
	 */
	BeanDescriptor getBeanDescriptor();
}
