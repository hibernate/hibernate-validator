/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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


