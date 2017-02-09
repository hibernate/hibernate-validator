/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.valueextraction.Unwrapping;

import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@TestForIssue(jiraKey = "HV-1237")
@SuppressWarnings("restriction")
public class NestedTypeArgumentsValueExtractorTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void validation_of_nested_type_arguments_works_with_map_of_list_of_optional() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( Foo.validFoo() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( Foo.invalidKeyFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[k].<map key>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( "<map key>", true, "k", null )
		);

		constraintViolations = validator.validate( Foo.invalidListFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key1].<map value>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( "<map value>", true, "key1", null )
		);

		constraintViolations = validator.validate( Foo.invalidStringFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key1].<map value>[0].<iterable element>",
				"map[key1].<map value>[1].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( "<map value>", true, "key1", null )
						.typeArgument( "<iterable element>", true, null, 0 ),
				pathWith()
						.property( "map" )
						.typeArgument( "<map value>", true, "key1", null )
						.typeArgument( "<iterable element>", true, null, 1 )
		);

		constraintViolations = validator.validate( Foo.reallyInvalidFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[k].<map key>",
				"map[k].<map value>",
				"map[k].<map value>[0].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class, Size.class, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( "<map key>", true, "k", null ),
				pathWith()
						.property( "map" )
						.typeArgument( "<map value>", true, "k", null ),
				pathWith()
						.property( "map" )
						.typeArgument( "<map value>", true, "k", null )
						.typeArgument( "<iterable element>", true, null, 0 )
		);
	}

	@Test
	public void validation_of_nested_type_arguments_works_with_map_of_list_of_stringproperty() {
		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( Bar.validBar() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( Bar.invalidStringPropertyBar() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key].<map value>[1].<iterable element>" );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( "<map value>", true, "key", null )
						.typeArgument( "<iterable element>", true, null, 1 )
		);

		constraintViolations = validator.validate( Bar.invalidListElementBar() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key].<map value>[0].<iterable element>" );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( "<map value>", true, "key", null )
						.typeArgument( "<iterable element>", true, null, 0 )
		);
	}

	private static class Foo {

		private Map<@Size(min = 2) String, @NotNull @Size(min = 2) List<Optional<@Size(min = 3) String>>> map;

		private static Foo validFoo() {
			Foo foo = new Foo();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key", list );

			return foo;
		}

		private static Foo invalidKeyFoo() {
			Foo foo = new Foo();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}

		private static Foo invalidListFoo() {
			Foo foo = new Foo();

			List<Optional<String>> list = Arrays.asList( Optional.of( "only one value" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static Foo invalidStringFoo() {
			Foo foo = new Foo();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ), Optional.of( "2" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static Foo reallyInvalidFoo() {
			Foo foo = new Foo();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}
	}

	private static class Bar {

		private Map<@Size(min = 2) String, List<@NotNull(payload = { Unwrapping.Skip.class }) @Size(min = 2) StringProperty>> map;

		private static Bar validBar() {
			Bar bar = new Bar();

			List<StringProperty> list = Arrays.asList( new SimpleStringProperty( "one" ), new SimpleStringProperty( "tw" ),
					new SimpleStringProperty( "three" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}

		private static Bar invalidStringPropertyBar() {
			Bar bar = new Bar();

			List<StringProperty> list = Arrays.asList( new SimpleStringProperty( "one" ), new SimpleStringProperty( "t" ),
					new SimpleStringProperty( "three" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}

		private static Bar invalidListElementBar() {
			Bar bar = new Bar();

			List<StringProperty> list = Arrays.asList( null, new SimpleStringProperty( "two" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}
	}

}
