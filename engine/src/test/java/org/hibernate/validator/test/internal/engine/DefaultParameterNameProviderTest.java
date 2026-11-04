/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import jakarta.validation.ParameterNameProvider;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Unit test for {@link DefaultParameterNameProvider}.
 *
 * @author Gunnar Morling
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultParameterNameProviderTest {

	private ParameterNameProvider parameterNameProvider;

	@BeforeAll
	public void setupParameterNameProvider() {
		parameterNameProvider = new DefaultParameterNameProvider();
	}

	@Test
	public void getParametersForParameterlessConstructor() throws Exception {
		assertEquals(
				buildExpectedArgumentNameList(),
				parameterNameProvider.getParameterNames( Foo.class.getConstructor() ) );
	}

	@Test
	public void getParametersForConstructorWithOneParameter() throws Exception {
		assertEquals(
				buildExpectedArgumentNameList( "bar" ),
				parameterNameProvider.getParameterNames( Foo.class.getConstructor( String.class ) ) );
	}

	@Test
	public void getParametersForConstructorWithSeveralParameters() throws Exception {
		assertEquals(
				buildExpectedArgumentNameList( "bar", "baz" ),
				parameterNameProvider.getParameterNames( Foo.class.getConstructor( String.class, String.class ) ) );
	}

	@Test
	public void getParametersForParameterlessMethod() throws Exception {
		assertEquals(
				buildExpectedArgumentNameList(),
				parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo" ) ) );
	}

	@Test
	public void getParametersForMethodWithOneParameter() throws Exception {
		assertEquals(
				buildExpectedArgumentNameList( "bar" ),
				parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo", String.class ) ) );
	}

	@Test
	public void getParametersForMethodWithSeveralParameters() throws Exception {
		assertEquals(
				buildExpectedArgumentNameList( "bar", "baz" ),
				parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo", String.class, String.class ) ) );
	}

	private List<String> buildExpectedArgumentNameList(String... names) {
		List<String> parameterNames = newArrayList();
		Collections.addAll( parameterNames, names );
		return parameterNames;
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
