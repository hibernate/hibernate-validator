/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-257")
public class ProxyTest {
	@Test
	public void testValidateA() {
		InvocationHandler handler = new CustomInvocationHandler( "some object" );

		A a = (A) Proxy.newProxyInstance( getClass().getClassLoader(), new Class<?>[] { A.class }, handler );
		assertEquals( Integer.valueOf( 0 ), a.getInteger() );

		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<A>> violations = validator.validate( a );
		assertNumberOfViolations( violations, 2 );
	}

	@Test
	public void testValidateB() {
		InvocationHandler handler = new CustomInvocationHandler( "some object" );

		B b = (B) Proxy.newProxyInstance( getClass().getClassLoader(), new Class<?>[] { B.class }, handler );
		assertEquals( Integer.valueOf( 0 ), b.getInteger() );

		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<B>> violations = validator.validate( b );
		assertNumberOfViolations( violations, 2 );
	}

	private class CustomInvocationHandler implements InvocationHandler {
		private Object o;

		public CustomInvocationHandler(Object o) {
			this.o = o;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ( method.getName().equals( "getInteger" ) ) {
				method.setAccessible( true );
				return 0;
			}
			if ( method.getName().equals( "getString" ) ) {
				return "a";
			}
			return method.invoke( o, args );
		}
	}
}


