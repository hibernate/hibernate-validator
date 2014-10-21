/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg;

import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;

/**
 * Represents a constraint mapping configured via the programmatic API.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface ConstraintMapping {
	/**
	 * Starts defining constraints on the specified bean class. Each bean class may only be configured once within all 
	 * constraint mappings used for configuring one validator factory.
	 *
	 * @param <C> The type to be configured.
	 * @param beanClass The bean class on which to define constraints. All constraints defined after calling this method
	 * are added to the bean of the type {@code beanClass} until the next call of {@code type}.
	 *
	 * @return Instance allowing for defining constraints on the specified class.
	 */
	<C> TypeConstraintMappingContext<C> type(Class<C> beanClass);
}
