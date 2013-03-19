/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.util.classfilter;

import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * A filter to be used when invoking
 * {@link ReflectionHelper#computeClassHierarchy(Class, ClassFilter...)}.
 * 
 * @author Gunnar Morling
 */
public interface ClassFilter {

	/**
	 * Whether the given class is accepted by this filter or not.
	 * 
	 * @param clazz
	 *            the class of interest
	 * @return {@code true} if this filter accepts the given class (meaning it
	 *         will be contained in the result of
	 *         {@link ReflectionHelper#computeClassHierarchy(Class, ClassFilter...)}
	 *         , {@code false} otherwise.
	 */
	boolean accepts(Class<?> clazz);
}
