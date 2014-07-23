/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.facets;

import java.lang.annotation.ElementType;
import java.util.Set;
import javax.validation.ElementKind;
import javax.validation.metadata.GroupConversionDescriptor;

/**
 * Provides a unified view on cascadable elements of all kinds, be it properties
 * of a Java bean, the arguments passed to an executable or the value returned
 * from an executable. Allows a unified handling of these elements in the
 * validation routine.
 *
 * @author Gunnar Morling
 */
public interface Cascadable {

	/**
	 * Converts the given validation group as per the group conversion
	 * configuration for this element (as e.g. specified via
	 * {@code @ConvertGroup}.
	 *
	 * @param originalGroup The group to convert.
	 *
	 * @return The converted group. Will be the original group itself in case no
	 *         conversion is to be performed.
	 */
	Class<?> convertGroup(Class<?> originalGroup);

	/**
	 * Returns a set with {@link GroupConversionDescriptor}s representing the
	 * group conversions of this cascadable.
	 *
	 * @return A set with group conversion descriptors. May be empty, but never
	 *         {@code null}.
	 */
	Set<GroupConversionDescriptor> getGroupConversionDescriptors();

	ElementType getElementType();

	/**
	 * Returns the name of this cascadable element.
	 *
	 * @return The name of this cascadable element.
	 */
	String getName();

	/**
	 * Returns the element kind of this cascadable.
	 *
	 * @return The kind of this cascadable.
	 */
	ElementKind getKind();
}
