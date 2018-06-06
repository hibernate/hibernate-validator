/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

import java.lang.annotation.ElementType;

/**
 * Facet of a constraint mapping creational context which allows to select the bean
 * property to which the next operations shall apply.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface PropertyTarget {
	/**
	 * Selects a property to which the next operations shall apply.
	 * <p>
	 * Until this method is called constraints apply on class level. After calling this method constraints
	 * apply on the specified property with the given access type.
	 * <p>
	 * A given property may only be configured once.
	 *
	 * @param property The property on which to apply the following constraints (Java Bean notation).
	 * @param type The access type (field/property).
	 *
	 * @return A creational context representing the selected property.
	 *
	 * @deprecated Since 6.1. Planned for removal. Use either {@link PropertyTarget#field(String)} or
	 * 		{@link PropertyTarget#getter(String)} instead.
	 */
	@Deprecated
	PropertyConstraintMappingContext property(String property, ElementType type);

	/**
	 * Selects a field to which the next operations shall apply.
	 * <p>
	 * Until this method is called constraints apply on class level. After calling this method constraints
	 * apply on the specified field property.
	 * <p>
	 * A given field may only be configured once.
	 *
	 * @param property The field name that represents a property on which to apply the following constraints.
	 *
	 * @return A creational context representing the selected field property.
	 */
	PropertyConstraintMappingContext field(String property);

	/**
	 * Selects a getter to which the next operations shall apply.
	 * <p>
	 * Until this method is called constraints apply on class level. After calling this method constraints
	 * apply on the specified getter property.
	 * <p>
	 * A given getter may only be configured once.
	 *
	 * @param property The getter property name (using the Java Bean notation, e.g. {@code name} to address {@code getName()})
	 * 		that represents a property on which to apply the following constraints.
	 *
	 * @return A creational context representing the selected getter property.
	 */
	PropertyConstraintMappingContext getter(String property);
}
