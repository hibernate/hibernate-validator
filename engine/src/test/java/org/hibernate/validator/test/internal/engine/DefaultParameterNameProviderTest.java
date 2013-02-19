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
package org.hibernate.validator.test.internal.engine;

import java.util.Collections;
import java.util.List;
import javax.validation.ParameterNameProvider;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.testng.Assert.assertEquals;

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
				buildExpectedArgumentNameList( "arg0" )
		);
	}

	@Test
	public void getParametersForConstructorWithSeveralParameters() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getConstructor( String.class, String.class ) ),
				buildExpectedArgumentNameList( "arg0", "arg1" )
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
				buildExpectedArgumentNameList( "arg0" )
		);
	}

	@Test
	public void getParametersForMethodWithSeveralParameters() throws Exception {
		assertEquals(
				parameterNameProvider.getParameterNames( Foo.class.getMethod( "foo", String.class, String.class ) ),
				buildExpectedArgumentNameList( "arg0", "arg1" )
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
