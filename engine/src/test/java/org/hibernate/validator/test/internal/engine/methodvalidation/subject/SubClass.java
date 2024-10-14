/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.subject;

/**
 * @author cbeckey@paypal.com
 */
public class SubClass extends ConcreteClass<ValueObjectSubClass> {

	@Override
	public String doSomething(ValueObjectSubClass vo) {
		return "subclass-" + vo.getName();
	}

}
