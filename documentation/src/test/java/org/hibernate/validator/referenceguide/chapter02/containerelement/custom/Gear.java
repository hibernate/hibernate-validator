/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.custom;

//end::include[]

//tag::include[]
public class Gear {
	private final Integer torque;

	public Gear(Integer torque) {
		this.torque = torque;
	}

	public Integer getTorque() {
		return torque;
	}

	public static class AcmeGear extends Gear {
		public AcmeGear() {
			super( 60 );
		}
	}
}
//end::include[]
