package org.hibernate.validator.referenceguide.chapter02.typeargument;

import java.lang.reflect.Type;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

public class FooUnwrapper extends ValidatedValueUnwrapper<Foo> {
	@Override
	public Object handleValidatedValue(Foo value) {
		return value.getT();
	}

	@Override
	public Type getValidatedValueType(Type valueType) {
		return String.class;
	}
}
