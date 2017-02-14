/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.facets;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;

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
	 * conversion is to be performed.
	 */
	Class<?> convertGroup(Class<?> originalGroup);

	/**
	 * Returns a set with {@link GroupConversionDescriptor}s representing the
	 * group conversions of this cascadable.
	 *
	 * @return A set with group conversion descriptors. May be empty, but never
	 * {@code null}.
	 */
	Set<GroupConversionDescriptor> getGroupConversionDescriptors();

	/**
	 * Returns the element type of the cascadable.
	 *
	 * @return Returns the element type of the cascadable.
	 */
	ElementType getElementType();

	/**
	 * Returns the data type of this cascadable, e.g. the type of a bean property or the
	 * return type of a method.
	 *
	 * @return This cascadable type.
	 */
	Type getCascadableType();

	/**
	 * Returns the value of this cacadable from the given parent.
	 */
	Object getValue(Object parent);

	/**
	 * Appends this cascadable element to the given path.
	 */
	void appendTo(PathImpl path);

	/**
	 * Returns the type parameters of the represented element that are marked for cascaded validation, if any. The
	 * returned list will contain the special {@link AnnotatedElement} marker in case the element itself has been
	 * marked.
	 */
	List<CascadingTypeParameter> getCascadingTypeParameters();

	public interface Builder {

		void addGroupConversions(Map<Class<?>, Class<?>> groupConversions);
		void addCascadingTypeParameters(List<CascadingTypeParameter> cascadingTypeParameters);
		Cascadable build();
	}
}
