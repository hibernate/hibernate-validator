/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to the select the bean
 * method to which the next operations shall apply.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface MethodTarget {
	/**
	 * Selects a method to which the next operations shall apply.
	 * <p>
	 * Until this method is called constraints apply on class level. After calling this method constraints
	 * apply to the specified method.
	 * </p>
	 * <p>
	 * A given method may only be configured once.
	 * </p>
	 *
	 * @param name The method name.
	 * @param parameterTypes The method parameter types.
	 *
	 * @return A creational context representing the selected method.
	 */
	MethodConstraintMappingContext method(String name, Class<?>... parameterTypes);
}
