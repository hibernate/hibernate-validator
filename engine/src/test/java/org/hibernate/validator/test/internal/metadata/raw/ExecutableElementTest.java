/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.raw;

import java.util.Date;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link ExecutableElement}.
 *
 * @author Gunnar Morling
 */
public class ExecutableElementTest {


	@Test
	public void executableAsStringShouldReturnMethodNameWithBracesForParameterlessMethod() throws Exception {
		assertEquals( ExecutableElement.getExecutableAsString( "foo" ), "foo()" );
		assertEquals( ExecutableElement.forMethod( Bar.class.getMethod( "zap" ) ).getAsString(), "zap()" );
		assertEquals( ExecutableElement.forConstructor( Bar.class.getConstructor() ).getAsString(), "Bar()" );
	}

	@Test
	public void executableAsStringShouldReturnMethodNameWithSimpleParamerTypeNames() throws Exception {
		assertEquals( ExecutableElement.getExecutableAsString( "foo", int.class, Bar.class ), "foo(int, Bar)" );
		assertEquals(
				ExecutableElement.forMethod( Bar.class.getMethod( "zap", int.class, Date.class ) ).getAsString(),
				"zap(int, Date)"
		);
		assertEquals(
				ExecutableElement.forConstructor( Bar.class.getConstructor( int.class, Date.class ) )
						.getAsString(), "Bar(int, Date)"
		);
	}

	public static class Bar {

		public Bar() {
		}

		public Bar(int i, Date date) {
		}

		public void Foo() {
		}

		public void zip() {
		}

		public void zap(int i) {
		}

		public void zap() {
		}

		public void zap(int i, Date date) {
		}
	}
}
