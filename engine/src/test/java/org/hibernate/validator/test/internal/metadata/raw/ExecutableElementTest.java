/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
