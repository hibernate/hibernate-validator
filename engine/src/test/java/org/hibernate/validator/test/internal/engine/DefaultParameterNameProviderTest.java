/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import jakarta.validation.ParameterNameProvider;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DefaultParameterNameProvider}.
 *
 * @author Gunnar Morling
 */
public class DefaultParameterNameProviderTest {

	private ParameterNameProvider parameterNameProvider;

	@BeforeClass
	public void setupParameterNameProvider() {
		parameterNameProvider = new DefaultParameterNameProvider();
	}

	@Test
	public void getParametersForParameterlessConstructor() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getConstructor() ),
				buildExpectedArgumentNameList()
		);
	}

	@Test
	public void getParametersForConstructorWithOneParameter() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getConstructor( String.class ) ),
				buildExpectedArgumentNameList( "bar" )
		);
	}

	@Test
	public void getParametersForConstructorWithSeveralParameters() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getConstructor( String.class, String.class ) ),
				buildExpectedArgumentNameList( "bar", "baz" )
		);
	}

	@Test
	public void getParametersForParameterlessMethod() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo" ) ),
				buildExpectedArgumentNameList()
		);
	}

	@Test
	public void getParametersForMethodWithOneParameter() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo", String.class ) ),
				buildExpectedArgumentNameList( "bar" )
		);
	}

	@Test
	public void getParametersForMethodWithSeveralParameters() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo", String.class, String.class ) ),
				buildExpectedArgumentNameList( "bar", "baz" )
		);
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
