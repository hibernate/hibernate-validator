/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.custom;

//end::include[]

//tag::include[]
public class Car {

	private GearBox<@MinTorque(100) Gear> gearBox;

	public void setGearBox(GearBox<Gear> gearBox) {
		this.gearBox = gearBox;
	}

	//...

}
//end::include[]
