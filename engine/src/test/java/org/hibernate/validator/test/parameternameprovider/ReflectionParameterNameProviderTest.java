/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.parameternameprovider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.validation.ParameterNameProvider;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider}.
 *
 * @author Khalid Alqinyah
 */
public class ReflectionParameterNameProviderTest {

	private ParameterNameProvider parameterNameProvider;

	@BeforeClass
	public void setup() {
		parameterNameProvider = new ReflectionParameterNameProvider();
	}

	@Test
	public void testConstructorParametersZeroParameters() throws Exception {
		List<String> expected = Collections.emptyList();
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getConstructor() );
		assertEquals( actual, expected, "Constructor with zero parameters does not match expected" );
	}

	@Test
	public void testConstructorParametersOneParameter() throws Exception {
		List<String> expected = Arrays.asList( "bar" );
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getConstructor( String.class ) );
		assertEquals( actual, expected, "Constructor with one parameter does not match expected" );
	}

	@Test
	public void testConstructorParametersTwoParameters() throws Exception {
		List<String> expected = Arrays.asList( "bar", "baz" );
		List<String> actual = parameterNameProvider.getParameterNames(
				Foo.class.getConstructor(
						String.class,
						String.class
				)
		);
		assertEquals( actual, expected, "Constructor with two parameters does not match expected" );
	}

	@Test
	public void testMethodParametersZeroParameters() throws Exception {
		List<String> expected = Collections.emptyList();
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo" ) );
		assertEquals( actual, expected, "Method with zero parameters does not match expected" );
	}

	@Test
	public void testMethodParametersOneParameter() throws Exception {
		List<String> expected = Arrays.asList( "bar" );
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo", String.class ) );
		assertEquals( actual, expected, "Method with one parameter does not match expected" );
	}

	@Test
	public void testMethodParametersTwoParameters() throws Exception {
		List<String> expected = Arrays.asList( "bar", "baz" );
		List<String> actual = parameterNameProvider.getParameterNames(
				Foo.class.getMethod(
						"foo",
						String.class,
						String.class
				)
		);
		assertEquals( actual, expected, "Method with two parameters does not match expected" );
	}

	@SuppressWarnings("unused")
	private static class Foo {
		public Foo() {
		}

		public Foo(String bar) {
		}

		public Foo(String bar, String baz) {
		}

		public void foo() {
		}

		public void foo(String bar) {
		}

		public void foo(String bar, String baz) {
		}
	}
}

