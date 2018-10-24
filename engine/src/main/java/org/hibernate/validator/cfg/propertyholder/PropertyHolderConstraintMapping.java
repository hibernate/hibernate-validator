/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.propertyholder;

/**
 * Represents a property holder constraint mapping configured via the programmatic API.
 *
 * @author Marko Bekhta
 */
public interface PropertyHolderConstraintMapping {

	/**
	 * Starts defining constraints for the specified unique mapping name. Each mapping name may only be used
	 * once within all property holder constraint mappings used for configuring one validator factory.
	 *
	 * @param propertyHolderMappingName The mapping name for which to define constraints. All constraints
	 * 		defined after calling this method are added to the bean of the type {@code beanClass} until the
	 * 		next call of {@link this#type(String)}.
	 *
	 * @return Instance allowing for defining constraints for the specified property holder mapping name.
	 */
	TypeConstraintMappingContext type(String propertyHolderMappingName);

}
