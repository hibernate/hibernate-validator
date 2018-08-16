/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.propertyholder;

/**
 * Constraint mapping creational context representing a type. Allows place
 * class-level constraints on that type, define its default group sequence (and provider)
 * and to navigate to other constraint targets.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface TypeConstraintMappingContext extends PropertyTarget, PropertyHolderTarget {

	/**
	 * Defines the default group sequence for current type.
	 *
	 * @param defaultGroupSequence the default group sequence.
	 *
	 * @return The current creational context following the method chaining pattern.
	 */
	TypeConstraintMappingContext defaultGroupSequence(Class<?>... defaultGroupSequence);
}
