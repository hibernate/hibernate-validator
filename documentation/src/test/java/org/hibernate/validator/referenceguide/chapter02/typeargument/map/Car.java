//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.typeargument.map;

//end::include[]

import java.util.EnumMap;

import javax.validation.Valid;

//tag::include[]
public class Car {

	public enum FuelConsumption {
		CITY,
		HIGHWAY
	}

	@Valid
	private EnumMap<FuelConsumption, @MaxAllowedFuelConsumption Integer> fuelConsumption = new EnumMap<>(FuelConsumption.class);

	public void setFuelConsumption(FuelConsumption consumption, int value) {
		fuelConsumption.put( consumption, value );
	}

	//...

}
//end::include[]
