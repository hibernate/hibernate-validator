package org.hibernate.validator.test.internal.engine.cascaded;

public class SomeReference<T> implements Reference<T> {

	private final T value;

	public SomeReference(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}
}
