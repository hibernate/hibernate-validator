/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

import jakarta.validation.ValidationException;

import org.hibernate.validator.Incubating;

/**
 * Facet of a constraint mapping creational context which allows to select a type argument or the component type of the
 * (return) type of the current property, parameter or method as target for the next operations.
 *
 * @author Gunnar Morling
 * @since 6.0
 */
@Incubating
public interface ContainerElementTarget {

	/**
	 * Selects the single type argument of the current element's generic type as the target for the next operations.
	 * Selects the component type if the current element is of an array type.
	 *
	 * @return A creational context representing the single type argument or the component type of the current element's
	 * type.
	 * @throws ValidationException If the given element (property, return value or parameter) is not of a generic type
	 * nor of an array type or is a generic type but has more than one type argument.
	 */
	ContainerElementConstraintMappingContext containerElementType();

	/**
	 * Selects the single type argument of the current element's generic type as the target for the next operations.
	 * Selects the component type if the current element is of an array type.
	 *
	 * @param index The index of the type argument to configure. Pass 0 when navigating into an array type.
	 * @param nestedIndexes the nested index(es) in case the container element to configure is a generic type within
	 * another generic type, e.g. {@code List<Map<String, String>>}, a multi-dimensional array or a combination of
	 * (nested) parameterized and array types.
	 * @return A creational context representing the specified type argument.
	 * @throws ValidationException If the given element (property, return value or parameter) is not of a generic type
	 * nor of an array type or is a generic type but has no type argument with the given index.
	 */
	ContainerElementConstraintMappingContext containerElementType(int index, int... nestedIndexes);
}
