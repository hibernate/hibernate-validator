/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.propertyholder;

import org.hibernate.validator.Incubating;

/**
 * Facet of a constraint mapping creational context which allows to select a type argument or the component type of the
 * (return) type of the current property as target for the next operations.
 *
 * @author Marko Bekhta
 */
@Incubating
public interface ContainerElementTarget {

	ContainerElementConstraintMappingContext containerElementType(Class<?> type);

	ContainerElementConstraintMappingContext containerElementType(Class<?> type, int index, int... nestedIndexes);

	CascadableContainerElementConstraintMappingContext containerElementType(String mapping);

	CascadableContainerElementConstraintMappingContext containerElementType(String mapping, int index, int... nestedIndexes);
}
