package org.hibernate.validator.referenceguide.chapter03.groups.groupsequenceprovider;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.referenceguide.chapter03.groups.CarChecks;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

public class RentalCarGroupSequenceProvider implements DefaultGroupSequenceProvider<RentalCar> {
	public List<Class<?>> getValidationGroups(RentalCar car) {
		List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();
		defaultGroupSequence.add( RentalCar.class );

		if ( car != null && !car.isRented() ) {
			defaultGroupSequence.add( CarChecks.class );
		}

		return defaultGroupSequence;
	}
}
