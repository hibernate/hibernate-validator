/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

/**
 * @author Guillaume Smet
 */
public class Wrapper2<T> implements IWrapper21<T>, IWrapper22<T> {

	private T property;

	public Wrapper2(T property) {
		this.property = property;
	}

	@Override
	public T getProperty() {
		return property;
	}
}
