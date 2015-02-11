/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.typeannotationconstraint;

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

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.testutil.constraints.NotBlankTypeUse;
import org.hibernate.validator.testutil.constraints.NotNullTypeUse;
import org.hibernate.validator.testutil.MessageLoggedAssertionLogger;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Tests Java 8 type use annotations.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 */
public class TypeAnnotationConstraintTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void field_constraint_provided_on_type_parameter_of_a_list_gets_validated() {
		A1 a = new A1();
		a.names = Arrays.asList( "First", "", null );

		Set<ConstraintViolation<A1>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "names[1]", "names[2]", "names[2]" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlankTypeUse.class,
				NotBlankTypeUse.class,
				NotNullTypeUse.class
		);
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000187.*")
	public void valid_annotation_required_for_constraint_on_type_parameter_of_iterable() {
		A2 a = new A2();
		a.names = Arrays.asList( "First", "", null );
		validator.validate( a );
	}

	@Test
	public void getter_constraint_provided_on_type_parameter_of_a_list_gets_validated() {
		A3 a = new A3();
		a.strings = Arrays.asList( "", "First", null );

		Set<ConstraintViolation<A3>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "strings[0]", "strings[2]", "strings[2]" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlankTypeUse.class,
				NotBlankTypeUse.class,
				NotNullTypeUse.class
		);
	}

	@Test
	public void constraint_provided_on_custom_bean_used_as_list_parameter_gets_validated() {
		B b = new B();
		b.bars = Arrays.asList( new Bar( 2 ), null );
		Set<ConstraintViolation<B>> constraintViolations = validator.validate( b );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "bars[1]", "bars[0].number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotNullTypeUse.class );
	}

	@Test
	public void constraint_specified_on_type_parameter_of_optional_gets_validated() {
		C c = new C();
		c.stringOptional = Optional.of( "" );

		Set<ConstraintViolation<C>> constraintViolations = validator.validate( c );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "stringOptional" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class );
	}

	@Test
	public void constraint_specified_on_value_type_of_map_gets_validated() {
		F f = new F();
		f.namesMap = newHashMap();
		f.namesMap.put( "first", "Name 1" );
		f.namesMap.put( "second", "" );
		f.namesMap.put( "third", "Name 3" );
		Set<ConstraintViolation<F>> constraintViolations = validator.validate( f );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "namesMap[second]" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000182.*")
	public void custom_generic_type_with_type_annotation_constraint_but_no_unwrapper_throws_exception() {
		// No unwrapper is registered for Baz
		BazHolder bazHolder = new BazHolder();
		bazHolder.baz = null;
		validator.validate( bazHolder );
	}

	@Test
	public void return_value_constraint_provided_on_type_parameter_of_a_list_gets_validated() throws Exception {
		Method method = E.class.getDeclaredMethod( "returnStrings" );
		Set<ConstraintViolation<E>> constraintViolations = validator.forExecutables().validateReturnValue(
				new E(),
				method,
				Arrays.asList( "First", "", null )
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"returnStrings.<return value>[1]",
				"returnStrings.<return value>[2]",
				"returnStrings.<return value>[2]"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlankTypeUse.class,
				NotBlankTypeUse.class,
				NotNullTypeUse.class
		);
	}

	@Test
	public void method_parameter_constraint_provided_as_type_parameter_of_a_list_gets_validated()
			throws Exception {
		Method method = H.class.getDeclaredMethod( "setValues", List.class, Optional.class );
		Object[] values = new Object[] { Arrays.asList( "", "First", null ), Optional.of( "" ) };

		Set<ConstraintViolation<H>> constraintViolations = validator.forExecutables().validateParameters(
				new H(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"setValues.arg0[0]",
				"setValues.arg0[2]",
				"setValues.arg0[2]",
				"setValues.arg1"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlankTypeUse.class,
				NotBlankTypeUse.class,
				NotNullTypeUse.class,
				NotBlankTypeUse.class
		);
	}

	@Test
	public void constructor_parameter_constraint_provided_on_type_parameter_of_a_list_gets_validated()
			throws Exception {
		Constructor<G> constructor = G.class.getDeclaredConstructor( List.class, Optional.class );
		Object[] values = new Object[] { Arrays.asList( "", "First", null ), Optional.of( "" ) };

		Set<ConstraintViolation<G>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"G.arg0[0]",
				"G.arg0[2]",
				"G.arg0[2]",
				"G.arg1"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlankTypeUse.class,
				NotBlankTypeUse.class,
				NotNullTypeUse.class,
				NotBlankTypeUse.class
		);
	}

	@Test
	public void unsupported_use_of_type_constraints_logs_warning() {
		Logger log4jRootLogger = Logger.getRootLogger();
		MessageLoggedAssertionLogger assertingLogger = new MessageLoggedAssertionLogger( "HV000188" );
		log4jRootLogger.addAppender( assertingLogger );

		// No unwrapper exception shouldn't be thrown, type use constraints are ignored
		FooHolder fooHolder = new FooHolder();
		fooHolder.foo = null;
		validator.validate( fooHolder );

		assertingLogger.assertMessageLogged();
		log4jRootLogger.removeAppender( assertingLogger );
	}

	static class A1 {
		@Valid
		List<@NotNullTypeUse @NotBlankTypeUse String> names;
	}

	static class A2 {
		List<@NotNullTypeUse @NotBlankTypeUse String> names;
	}

	static class B {
		@Valid
		List<@NotNullTypeUse Bar> bars;
	}

	static class C {
		Optional<@NotBlankTypeUse String> stringOptional;
	}

	static class A3 {
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
		public G(@Valid List<@NotNullTypeUse @NotBlankTypeUse String> names, Optional<@NotBlankTypeUse String> optionalParameter) {

		}
	}

	static class H {
		public void setValues(@Valid List<@NotNullTypeUse @NotBlankTypeUse String> listParameter, Optional<@NotBlankTypeUse String> optionalParameter) {

		}
	}

	static class Bar {
		@Min(4)
		Integer number;

		public Bar(Integer number) {
			this.number = number;
		}
	}

	static class BazHolder {
		Baz<@NotNullTypeUse String> baz;
	}

	class Baz<T> {

	}

	class FooHolder {
		Foo<@NotNullTypeUse Integer, @NotBlankTypeUse String> foo;
	}

	class Foo<T, V> {

	}
}
