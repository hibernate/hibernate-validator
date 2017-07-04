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
			super( 100 );
		}
	}
}
//end::include[]
