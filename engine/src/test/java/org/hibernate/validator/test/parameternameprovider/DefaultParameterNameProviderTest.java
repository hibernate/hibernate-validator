/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.parameternameprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ParameterNameProvider;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for {@link org.hibernate.validator.internal.engine.DefaultParameterNameProvider}.
 *
 * @author Khalid Alqinyah
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultParameterNameProviderTest {

	private ParameterNameProvider parameterNameProvider;

	@BeforeAll
	public void setup() {
		parameterNameProvider = new DefaultParameterNameProvider();
	}

	@Test
	public void testConstructorParametersZeroParameters() throws Exception {
		List<String> expected = Collections.emptyList();
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getConstructor() );
		assertEquals( expected, actual, "Constructor with zero parameters does not match expected" );
	}

	@Test
	public void testConstructorParametersOneParameter() throws Exception {
		List<String> expected = Arrays.asList( "bar" );
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getConstructor( String.class ) );
		assertEquals( expected, actual, "Constructor with one parameter does not match expected" );
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
		assertEquals( expected, actual, "Constructor with two parameters does not match expected" );
	}

	@Test
	public void testMethodParametersZeroParameters() throws Exception {
		List<String> expected = Collections.emptyList();
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo" ) );
		assertEquals( expected, actual, "Method with zero parameters does not match expected" );
	}

	@Test
	public void testMethodParametersOneParameter() throws Exception {
		List<String> expected = Arrays.asList( "bar" );
		List<String> actual = parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo", String.class ) );
		assertEquals( expected, actual, "Method with one parameter does not match expected" );
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
		assertEquals( expected, actual, "Method with two parameters does not match expected" );
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
