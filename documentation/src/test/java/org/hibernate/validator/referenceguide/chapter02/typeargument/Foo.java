package org.hibernate.validator.referenceguide.chapter02.typeargument;

public class Foo<T> {
	T t;

	public Foo(T t) {
		this.t = t;
	}

	T getT() {
		return t;
	}
}
