package org.hibernate.validator.referenceguide.chapter02.typeargument;

public class GearBox<T extends Gear> {

	private final T gear;

	public GearBox(T gear) {
		this.gear = gear;
	}

	public Gear getGear() {
		return this.gear;
	}
}
