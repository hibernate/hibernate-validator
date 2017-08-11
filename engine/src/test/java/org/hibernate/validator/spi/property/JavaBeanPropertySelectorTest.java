/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.spi.property;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class JavaBeanPropertySelectorTest {

	private final JavaBeanPropertySelector selector = new JavaBeanPropertySelector();

	@Test
	public void isGetter() throws Exception {
		assertTrue( selector.isGetterMethod( lookup( "getInt" ) ) );
		assertFalse( selector.isGetterMethod( lookup( "getInt", boolean.class ) ) );
		assertFalse( selector.isGetterMethod( lookup( "getFoo" ) ) );
		assertFalse( selector.isGetterMethod( lookup( "setInt", int.class ) ) );
		assertFalse( selector.isGetterMethod( lookup( "foo" ) ) );
	}

	@Test
	public void propertyName() throws Exception {
		assertEquals( selector.getPropertyName( lookup( "getInt" ) ), "int" );
		// TODO: getFoo() is it really a getter, still return property name ?
		assertEquals( selector.getPropertyName( lookup( "getFoo" ) ), "foo" );
		assertNull( selector.getPropertyName( lookup( "setInt", int.class ) ) );
	}

	@Test
	public void findMethod() throws Exception {
		assertEquals( selector.findMethod( MyBean.class, "int" ), lookup( "getInt" ) );
		assertEquals( selector.findMethod( MyBean.class, "foo" ), lookup( "getFoo" ) );
	}

	private static <T, P> Method lookup(String name, Class<?> ... params) throws NoSuchMethodException {
		return MyBean.class.getMethod( name, params );
	}

	private interface MyBean {
		int getInt();

		// NOT a getter
		int getInt(boolean arg);

		void getFoo();

		int foo();

		int setInt(int value);

	}
}
