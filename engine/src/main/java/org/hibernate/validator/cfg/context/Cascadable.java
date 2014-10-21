/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to mark the underlying
 * element as to be validated in a cascaded way.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface Cascadable<C extends Cascadable<C>> {

	/**
	 * Marks the current element (property, parameter etc.) as cascadable.
	 *
	 * @return The current creational context following the method chaining pattern.
	 */
	C valid();

	/**
	 * Adds a group conversion for this cascadable element. Several conversions may be configured for one element.
	 *
	 * @param from the source group of the conversion to be configured
	 *
	 * @return a creational context allow to set the target group of the conversion
	 */
	GroupConversionTargetContext<C> convertGroup(Class<?> from);
}
