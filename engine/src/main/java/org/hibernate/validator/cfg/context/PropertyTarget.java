/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

import java.lang.annotation.ElementType;

/**
 * Facet of a constraint mapping creational context which allows to the select the bean
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
	 * </p>
	 * <p>
	 * A given property may only be configured once.
	 * </p>
	 *
	 * @param property The property on which to apply the following constraints (Java Bean notation).
	 * @param type The access type (field/property).
	 *
	 * @return A creational context representing the selected property.
	 */
	PropertyConstraintMappingContext property(String property, ElementType type);
}
