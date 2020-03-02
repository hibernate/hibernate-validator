/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.proxy;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

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
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "string" ),
				violationOf( Min.class ).withProperty( "integer" )
		);
	}

	@Test
	public void testValidateB() {
		InvocationHandler handler = new CustomInvocationHandler( "some object" );

		B b = (B) Proxy.newProxyInstance( getClass().getClassLoader(), new Class<?>[] { B.class }, handler );
		assertEquals( Integer.valueOf( 0 ), b.getInteger() );

		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<B>> violations = validator.validate( b );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "string" ),
				violationOf( Min.class ).withProperty( "integer" )
		);
	}

	private class CustomInvocationHandler implements InvocationHandler {
		private Object o;

		public CustomInvocationHandler(Object o) {
			this.o = o;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ( "getInteger".equals( method.getName() ) ) {
				method.setAccessible( true );
				return 0;
			}
			if ( "getString".equals( method.getName() ) ) {
				return "a";
			}
			return method.invoke( o, args );
		}
	}
}


