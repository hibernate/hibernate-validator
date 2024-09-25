/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import java.util.List;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

public class RentalCarGroupSequenceProvider implements DefaultGroupSequenceProvider<RentalCar> {
	@Override
	public List<Class<?>> getValidationGroups(RentalCar car) {
		return null;
	}
}
