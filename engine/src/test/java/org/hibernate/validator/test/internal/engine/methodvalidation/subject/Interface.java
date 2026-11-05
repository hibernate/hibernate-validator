package org.hibernate.validator.test.internal.engine.methodvalidation.subject;

/**
 * @author cbeckey@paypal.com
 */
public interface Interface<T extends ValueObject> {

	String doSomething(T vo);

}
