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
package org.hibernate.validator.test.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Date;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.testutil.TestForIssue;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link ExecutableHelper}.
 *
 * @author Gunnar Morling
 */
public class ExecutableHelperTest {

	private final ExecutableHelper executableHelper = new ExecutableHelper( new TypeResolutionHelper() );

	@Test
	@TestForIssue(jiraKey = "HV-818")
	public void testOverrides() throws Exception {
		Method getBar = Qax.class.getMethod( "getBar" );
		Method getBarString = Qax.class.getMethod( "getBar", String.class );

		Method getSubTypeBar = SubQax.class.getMethod( "getBar" );
		Method getSubTypeBarString = SubQax.class.getMethod( "getBar", String.class );
		Method getBarInteger = SubQax.class.getMethod( "getBar", Integer.class );

		Method getFooLong = SubQax.class.getMethod( "getFoo", Long.class );
		Method getStaticFoo = SubQax.class.getMethod( "getFoo" );
		Method getStaticFooString = SubQax.class.getMethod( "getFoo", String.class );
		Method getStaticFooInteger = SubQax.class.getMethod( "getFoo", Integer.class );
		Method getSuperTypeStaticFoo = Qax.class.getMethod( "getFoo" );

		assertTrue( executableHelper.overrides( getSubTypeBar, getBar ) );
		assertTrue( executableHelper.overrides( getSubTypeBarString, getBarString ) );

		assertFalse( executableHelper.overrides( getBar, getBarString ) );
		assertFalse( executableHelper.overrides( getBar, getBarInteger ) );
		assertFalse( executableHelper.overrides( getBarString, getBarInteger ) );
		assertFalse( executableHelper.overrides( getSubTypeBar, getBarInteger ) );
		assertFalse( executableHelper.overrides( getSubTypeBar, getBarString ) );
		assertFalse( executableHelper.overrides( getSubTypeBarString, getBarInteger ) );
		assertFalse( executableHelper.overrides( getSubTypeBarString, getBar ) );
		assertFalse( executableHelper.overrides( getSubTypeBarString, getSubTypeBar ) );

		assertFalse( executableHelper.overrides( getStaticFoo, getStaticFooString ) );
		assertFalse( executableHelper.overrides( getStaticFoo, getStaticFooInteger ) );
		assertFalse( executableHelper.overrides( getStaticFooString, getStaticFooInteger ) );
		assertFalse( executableHelper.overrides( getFooLong, getStaticFoo ) );
		assertFalse( executableHelper.overrides( getFooLong, getStaticFooInteger ) );
		assertFalse( executableHelper.overrides( getFooLong, getStaticFooString ) );
		assertFalse( executableHelper.overrides( getStaticFoo, getSuperTypeStaticFoo ) );
	}

	@Test
	public void methodFromSubTypeOverridesSuperTypeMethod() throws Exception {
		Method methodFromBase = Foo.class.getDeclaredMethod( "zap" );
		Method methodFromImpl = Bar.class.getDeclaredMethod( "zap" );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ), ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();
	}

	@Test
	public void methodFromSubTypeOverridesInterfaceTypeMethod() throws Exception {
		Method methodFromBase = IBaz.class.getDeclaredMethod( "zap" );
		Method methodFromImpl = Baz.class.getDeclaredMethod( "zap" );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ), ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();
	}

	@Test
	public void methodFromSuperTypeDoesNotOverrideSubTypeMethod() throws Exception {
		Method methodFromBase = Foo.class.getDeclaredMethod( "zap" );
		Method methodFromImpl = Bar.class.getDeclaredMethod( "zap" );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromBase ),
						ExecutableElement.forMethod( methodFromImpl )
				)
		).isFalse();
	}

	@Test
	public void methodWithDifferentNameDoesNotOverride() throws Exception {
		Method methodFromBase = Foo.class.getDeclaredMethod( "zap" );
		Method methodFromImpl = Bar.class.getDeclaredMethod( "zip" );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodWithDifferentParameterTypesDoesNotOverride() throws Exception {
		Method methodFromBase = Foo.class.getDeclaredMethod( "zap" );
		Method methodFromImpl = Bar.class.getDeclaredMethod( "zap", int.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodDefinedInOtherTypeHierarchyDoesNotOverride() throws Exception {
		Method first = Foo.class.getDeclaredMethod( "zap" );
		Method other = Baz.class.getDeclaredMethod( "zap" );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( other ),
						ExecutableElement.forMethod( first )
				)
		).isFalse();
	}

	@Test
	public void constructorDoesNotOverride() throws Exception {
		Constructor<Foo> first = Foo.class.getConstructor();
		Constructor<Bar> other = Bar.class.getConstructor();

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forConstructor( other ),
						ExecutableElement.forConstructor( first )
				)
		).isFalse();
	}

	@Test
	public void methodNamedAsConstructorDoesNotOverride() throws Exception {
		Constructor<Foo> first = Foo.class.getConstructor();
		Method other = Bar.class.getDeclaredMethod( "Foo" );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( other ),
						ExecutableElement.forConstructor( first )
				)
		).isFalse();
	}

	@Test
	public void methodWithNarrowedParameterTypeDoesNotOverride() throws Exception {
		Method methodFromBase = SimpleServiceBase.class.getDeclaredMethods()[0];
		Method methodFromImpl = SimpleServiceImpl1.class.getDeclaredMethod( "doSomething", Number.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = SimpleServiceImpl1.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}


	@Test
	public void methodWithIntermediateClass() throws Exception {
		Method methodFromBase = SimpleServiceBase.class.getDeclaredMethods()[0];
		Method methodFromImpl = SimpleServiceImpl2.class.getDeclaredMethod( "doSomething", Number.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = SimpleServiceImpl2.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodWithGenerics() throws Exception {
		Method methodFromBase = GenericServiceBase.class.getDeclaredMethods()[0];
		Method methodFromImpl = GenericServiceImpl1.class.getDeclaredMethod( "doSomething", Number.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = GenericServiceImpl1.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodWithGenericsAndIntermediateClass() throws Exception {
		Method methodFromBase = GenericServiceBase.class.getDeclaredMethods()[0];
		Method methodFromImpl = GenericServiceImpl2.class.getDeclaredMethod( "doSomething", Number.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = GenericServiceImpl2.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl )
						, ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodWithGenericsAndMultipleIntermediateClasses() throws Exception {
		Method methodFromBase = GenericServiceBase.class.getDeclaredMethods()[0];
		Method methodFromImpl = GenericServiceImpl3.class.getDeclaredMethod( "doSomething", Number.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = GenericServiceImpl2.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodWithParameterizedSubType() throws Exception {
		Method methodFromBase = GenericServiceBase.class.getDeclaredMethods()[0];
		Method methodFromImpl = ParameterizedSubType.class.getDeclaredMethod( "doSomething", Object.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = ParameterizedSubType.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodWithGenericInterface() throws Exception {
		Method methodFromBase = GenericInterface.class.getDeclaredMethods()[0];
		Method methodFromImpl = GenericInterfaceImpl1.class.getDeclaredMethod( "doSomething", Number.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = GenericInterfaceImpl1.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl )
						, ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void methodWithWildcard() throws Exception {
		Method methodFromBase = WildcardInterface.class.getDeclaredMethods()[0];
		Method methodFromImpl = WildcardInterfaceImpl.class.getDeclaredMethod( "doSomething", Integer.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isTrue();

		methodFromImpl = WildcardInterfaceImpl.class.getDeclaredMethod( "doSomething", Long.class );

		assertThat(
				executableHelper.overrides(
						ExecutableElement.forMethod( methodFromImpl ),
						ExecutableElement.forMethod( methodFromBase )
				)
		).isFalse();
	}

	@Test
	public void executableAsStringShouldReturnMethodNameWithBracesForParameterlessMethod() throws Exception {
		assertEquals( ExecutableElement.getExecutableAsString( "foo" ), "foo()" );
		assertEquals( ExecutableElement.forMethod( Foo.class.getMethod( "zap" ) ).getAsString(), "zap()" );
		assertEquals( ExecutableElement.forConstructor( Bar.class.getConstructor() ).getAsString(), "Bar()" );
	}

	@Test
	public void executableAsStringShouldReturnMethodNameWithSimpleParamerTypeNames() throws Exception {
		assertEquals( ExecutableElement.getExecutableAsString( "foo", int.class, Foo.class ), "foo(int, Foo)" );
		assertEquals(
				ExecutableElement.forMethod( Bar.class.getMethod( "zap", int.class, Date.class ) ).getAsString(),
				"zap(int, Date)"
		);
		assertEquals(
				ExecutableElement.forConstructor( Bar.class.getConstructor( int.class, Date.class ) )
						.getAsString(), "Bar(int, Date)"
		);
	}

	public abstract static class GenericServiceBase<T> {
		public abstract void doSomething(T t);
	}

	public static class GenericServiceImpl1 extends GenericServiceBase<Number> {
		@Override
		public void doSomething(Number t) {
		}

		public void doSomething(Integer t) {
		}
	}

	public abstract static class GenericServiceBaseExt extends GenericServiceBase<Number> {
	}

	public abstract static class GenericParameterizedServiceBaseExt<D> extends GenericServiceBase<D> {
	}

	public static class GenericServiceImpl2 extends GenericServiceBaseExt {
		@Override
		public void doSomething(Number t) {
		}

		public void doSomething(Integer t) {
		}
	}

	public static class GenericServiceImpl3 extends GenericParameterizedServiceBaseExt<Number> {
		@Override
		public void doSomething(Number t) {
		}

		public void doSomething(Integer t) {
		}
	}

	public abstract static class SimpleServiceBase {
		public abstract void doSomething(Number t);
	}

	public abstract static class SimpleServiceBaseExt extends SimpleServiceBase {
	}

	public static class SimpleServiceImpl1 extends SimpleServiceBase {
		@Override
		public void doSomething(Number t) {
		}

		public void doSomething(Integer t) {
		}
	}

	public static class SimpleServiceImpl2 extends SimpleServiceBaseExt {
		@Override
		public void doSomething(Number t) {
		}

		public void doSomething(Integer t) {
		}
	}

	public static class ParameterizedSubType<U> extends GenericServiceBase<U> {

		@Override
		public void doSomething(U t) {
		}

		public void doSomething(Integer t) {
		}
	}

	public interface GenericInterface<T> {
		void doSomething(T t);
	}

	public static class GenericInterfaceImpl1 implements GenericInterface<Number> {
		@Override
		public void doSomething(Number t) {
		}

		public void doSomething(Integer t) {
		}
	}

	public interface WildcardInterface<T extends Number> {
		void doSomething(T t);
	}

	public static class WildcardInterfaceImpl implements WildcardInterface<Integer> {
		@Override
		public void doSomething(Integer t) {
		}

		public void doSomething(Long t) {
		}
	}

	public static class Foo {

		public Foo() {
		}

		public void zap() {
		}
	}

	public static class Bar extends Foo {

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

		@Override
		public void zap() {
		}

		public void zap(int i, Date date) {
		}
	}

	public interface IBaz {
		void zap();
	}

	public static class Baz implements IBaz {

		@Override
		public void zap() {
		}
	}

	@SuppressWarnings("unused")
	private static class Qax {
		public String getBar() {
			return null;
		}

		public String getBar(String param) {
			return null;
		}

		public static String getFoo() {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class SubQax extends Qax {
		@Override
		public String getBar() {
			return null;
		}

		@Override
		public String getBar(String param) {
			return null;
		}

		public String getBar(Integer param) {
			return null;
		}

		public String getFoo(Long param) {
			return null;
		}

		public static String getFoo() {
			return null;
		}

		public static String getFoo(Integer param) {
			return null;
		}

		public static String getFoo(String param) {
			return null;
		}
	}
}
