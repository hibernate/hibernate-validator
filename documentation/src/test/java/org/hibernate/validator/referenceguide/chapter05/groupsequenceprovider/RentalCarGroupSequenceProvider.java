/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter05.groupsequenceprovider;

//end::include[]
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.referenceguide.chapter05.CarChecks;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

//tag::include[]
public class RentalCarGroupSequenceProvider
		implements DefaultGroupSequenceProvider<RentalCar> {

	@Override
	public List<Class<?>> getValidationGroups(Class<?> klass, RentalCar car) {
		List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();
		defaultGroupSequence.add( RentalCar.class );

		if ( car != null && !car.isRented() ) {
			defaultGroupSequence.add( CarChecks.class );
		}

		return defaultGroupSequence;
	}
}
//end::include[]
