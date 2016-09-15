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
	public List<Class<?>> getValidationGroups(RentalCar car) {
		List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();
		defaultGroupSequence.add( RentalCar.class );

		if ( car != null && !car.isRented() ) {
			defaultGroupSequence.add( CarChecks.class );
		}

		return defaultGroupSequence;
	}
}
//end::include[]
