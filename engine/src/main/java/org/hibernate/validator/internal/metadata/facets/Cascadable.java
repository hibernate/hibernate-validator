/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.facets;

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.util.Set;
import javax.validation.ElementKind;
import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;

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

	/**
	 * Returns the type arguments constraints for this cascadable.
	 *
	 * @return the type arguments constraints for this cascadable, or an empty set if no constrained type arguments are
	 * found
	 */
	Set<MetaConstraint<?>> getTypeArgumentsConstraints();

	/**
	 * Defines how the validated values needs to be treated in case there is a potential unwrapper specified for its type
	 *
	 * @return the {@code ValidatedValueUnwrapMode} to be used for this constraint.
	 */
	UnwrapMode unwrapMode();

	/**
	 * Returns the data type of this cascadable, e.g. the type of a bean property or the
	 * return type of a method.
	 *
	 * @return This cascadable type.
	 */
	Type getType();
}
