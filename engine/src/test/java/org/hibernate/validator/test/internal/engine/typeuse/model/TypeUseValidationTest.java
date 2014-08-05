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
package org.hibernate.validator.test.internal.engine.typeuse.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.Min;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.util.constraints.NotBlankTypeUse;
import org.hibernate.validator.test.internal.util.constraints.NotNullTypeUse;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * @author Khalid Alqinyah
 */
public class TypeUseValidationTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testTypeUseWithList() {
		A a = new A();
		a.names = Arrays.asList( "First", "", null );
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( a );
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "names[1]", "names[2]", "names[2]" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class, NotBlankTypeUse.class, NotNullTypeUse.class );
	}

	@Test
	public void testTypeUseWithMap() {
		F f = new F();
		f.namesMap = newHashMap();
		f.namesMap.put("first", "Name 1");
		f.namesMap.put("second", "");
		f.namesMap.put("third", "Name 3");
		Set<ConstraintViolation<F>> constraintViolations = validator.validate( f );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "namesMap[second]" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class );
	}

	@Test
	public void testTypeUseWithCustomBean() {
		B b = new B();
		b.bars = Arrays.asList( new Bar( 2 ), null );
		Set<ConstraintViolation<B>> constraintViolations = validator.validate( b );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "bars[1]", "bars[0].number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotNullTypeUse.class );
	}

	@Test
	public void testTypeUseWithOptional() {
		C c = new C();
		c.stringOptional = Optional.of( "" );
		Set<ConstraintViolation<C>> constraintViolations = validator.validate( c );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "stringOptional" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class );
	}

	@Test( expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000182.*" )
	public void testTypeUseWithCustomType() {
		// No unwrapper is registered for Baz
		BazHolder bazHolder = new BazHolder();
		bazHolder.baz = null;
		validator.validate( bazHolder );
	}

	@Test
	public void testTypeUseWithGetter() {
		D d = new D();
		d.strings = Arrays.asList( "", "First", null );
		Set<ConstraintViolation<D>> constraintViolations = validator.validate( d );
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "strings[0]", "strings[2]", "strings[2]" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class, NotBlankTypeUse.class, NotNullTypeUse.class );
	}

	@Test
	public void testTypeUseWithReturnValue() throws Exception {
		Method method = E.class.getDeclaredMethod( "returnStrings" );
		Set<ConstraintViolation<E>> constraintViolations = validator.forExecutables().validateReturnValue(
				new E(),
				method,
				Arrays.asList( "First", "", null ) );
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations,
				"returnStrings.<return value>[1]",
				"returnStrings.<return value>[2]",
				"returnStrings.<return value>[2]" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class, NotBlankTypeUse.class, NotNullTypeUse.class );
	}

	@Test
	public void testTypeUseWithExecutableParameter() throws Exception {
		Method method = H.class.getDeclaredMethod( "setValues", List.class, Optional.class );
		Object[] values = new Object[] {Arrays.asList( "", "First", null ), Optional.of( "" ) };

		Set<ConstraintViolation<H>> constraintViolations = validator.forExecutables().validateParameters(
				new H(),
				method,
				values );
		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectPropertyPaths( constraintViolations,
				"setValues.arg0[0]",
				"setValues.arg0[2]",
				"setValues.arg0[2]",
				"setValues.arg1" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class, NotBlankTypeUse.class, NotNullTypeUse.class, NotBlankTypeUse.class );
	}

	@Test
	public void testTypeUseWithConstructorParameter() throws Exception {
		Constructor<G> constructor = G.class.getDeclaredConstructor( List.class, Optional.class );
		Object[] values = new Object[] {Arrays.asList( "", "First", null ), Optional.of( "" ) };

		Set<ConstraintViolation<G>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectPropertyPaths( constraintViolations,
				"G.arg0[0]",
				"G.arg0[2]",
				"G.arg0[2]",
				"G.arg1" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class, NotBlankTypeUse.class, NotNullTypeUse.class, NotBlankTypeUse.class );
	}

	@Test
	public void testTypeUseWithMoreThanOneTypeArgument() {
		// No unwrapper exception shouldn't be thrown, type use constraints are ignored
		FooHolder fooHolder = new FooHolder();
		fooHolder.foo = null;
		validator.validate( fooHolder );
	}

	@Test
	public void testTypeUseWithoutValidAnnotation() {
		I i = new I();
		i.names = Arrays.asList( "First", "", null );
		Set<ConstraintViolation<I>> constraintViolations = validator.validate( i );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	static class A {
		@Valid
		List<@NotNullTypeUse @NotBlankTypeUse String> names;
	}

	static class B {
		@Valid
		List<@NotNullTypeUse Bar> bars;
	}

	static class C {
		@Valid
		Optional<@NotBlankTypeUse String> stringOptional;
	}

	static class D {
		List<String> strings;

		@Valid
		public List<@NotNullTypeUse @NotBlankTypeUse String> getStrings() {
			return strings;
		}
	}

	static class E {
		List<String> strings;

		@Valid
		public List<@NotNullTypeUse @NotBlankTypeUse String> returnStrings() {
			return strings;
		}
	}

	static class F {
		@Valid
		Map<String, @NotBlankTypeUse String> namesMap;
	}

	static class G {
		public G(@Valid List<@NotNullTypeUse @NotBlankTypeUse String> names, @Valid Optional<@NotBlankTypeUse String> optionalParameter) {

		}
	}

	static class H {
		public void setValues(@Valid List<@NotNullTypeUse @NotBlankTypeUse String> listParameter, @Valid Optional<@NotBlankTypeUse String> optionalParameter) {

		}
	}

	static class I {
		List<@NotNullTypeUse @NotBlankTypeUse String> names;
	}

	static class Bar {
		@Min(4)
		Integer number;

		public Bar(Integer number) {
			this.number = number;
		}
	}

	static class BazHolder {
		@Valid
		Baz<@NotNullTypeUse String> baz;
	}

	class Baz<T> {

	}

	class FooHolder {
		@Valid
		Foo<@NotNullTypeUse Integer, @NotBlankTypeUse String> foo;
	}

	class Foo<T, V> {

	}
}
