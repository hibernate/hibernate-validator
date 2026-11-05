package org.hibernate.validator.test.internal.engine.methodvalidation.subject;

/**
 * @author cbeckey@paypal.com
 */
public class ConcreteClass<T extends ValueObject> implements Interface<T> {

	@Override
	public String doSomething(T vo) {
		return "class-" + vo.getName();
	}

}
