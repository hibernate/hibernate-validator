/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
