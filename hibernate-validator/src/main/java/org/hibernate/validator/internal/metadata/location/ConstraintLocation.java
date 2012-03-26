/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.internal.metadata.location;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * Implementations describe the location at which a constraint is specified (a
 * bean, a method parameter etc.).
 *
 * @author Gunnar Morling
 */
public interface ConstraintLocation {

	Class<?> getBeanClass();

	/**
	 * Returns the type of the element at this constraint location. Depending
	 * on the concrete implementation this might be the type of an annotated bean, method parameter etc.
	 *
	 * @return The type of the element at this constraint location.
	 */
	Type typeOfAnnotatedElement();

	ElementType getElementType();

	Member getMember();
}
