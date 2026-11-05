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
