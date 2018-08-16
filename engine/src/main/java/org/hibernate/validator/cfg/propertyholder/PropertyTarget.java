/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.propertyholder;

/**
 * Facet of a property holder constraint mapping creational context which allows to specify the
 * property to which the next operations should be apply.
 *
 * @author Marko Bekhta
 */
public interface PropertyTarget {

	/**
	 * Defines a property to which the next operations shall apply.
	 * <p>
	 * Until this method is called constraints apply on property holder level. After calling this method constraints
	 * apply on the specified property with the given property type.
	 * </p>
	 * <p>
	 * A given property may only be configured once.
	 * </p>
	 *
	 * @param property The property on which to apply the following constraints.
	 * @param propertyType The type of the specified property.
	 *
	 * @return A creational context representing the selected property.
	 */
	PropertyConstraintMappingContext property(String property, Class<?> propertyType);

}
