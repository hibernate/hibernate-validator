package org.hibernate.validator.referenceguide.chapter02.typeargument;

import java.lang.reflect.Type;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

public class GearBoxUnwrapper extends ValidatedValueUnwrapper<GearBox> {
	@Override
	public Object handleValidatedValue(GearBox gearBox) {
		return gearBox == null ? null : gearBox.getGear();
	}

	@Override
	public Type getValidatedValueType(Type valueType) {
		return Gear.class;
	}
}
