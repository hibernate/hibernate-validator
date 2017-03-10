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

import org.hibernate.validator.internal.engine.path.NodeImpl;
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
		Set<ConstraintViolation<MapOfLists>> constraintViolations = validator.validate( MapOfLists.validFoo() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( MapOfLists.invalidKeyFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map<K>[k].<map key>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, "k", null, Map.class, 0 )
		);

		constraintViolations = validator.validate( MapOfLists.invalidListFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key1].<map value>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
		);

		constraintViolations = validator.validate( MapOfLists.invalidStringFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key1].<map value>[0].<iterable element>",
				"map[key1].<map value>[1].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 ),
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, List.class, 0 )
		);

		constraintViolations = validator.validate( MapOfLists.reallyInvalidFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map<K>[k].<map key>",
				"map[k].<map value>",
				"map[k].<map value>[0].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class, Size.class, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, "k", null, Map.class, 0 ),
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "k", null, Map.class, 1 ),
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "k", null, Map.class, 1 )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
		);
	}

	@Test
	public void validation_of_nested_type_arguments_works_with_map_of_list_of_stringproperty() {
		Set<ConstraintViolation<MapOfListsWithAutomaticUnwrapping>> constraintViolations = validator.validate( MapOfListsWithAutomaticUnwrapping.validBar() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( MapOfListsWithAutomaticUnwrapping.invalidStringPropertyBar() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key].<map value>[1].<iterable element>" );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key", null, Map.class, 1 )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, List.class, 0 )
		);

		constraintViolations = validator.validate( MapOfListsWithAutomaticUnwrapping.invalidListElementBar() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key].<map value>[0].<iterable element>" );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key", null, Map.class, 1 )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
		);
	}

	@Test
	public void validation_of_nested_type_arguments_works_with_array_of_optional_of_stringproperty() {
		Set<ConstraintViolation<ArrayOfOptionalsWithAutomaticUnwrapping>> constraintViolations = validator.validate( ArrayOfOptionalsWithAutomaticUnwrapping.validBaz() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( ArrayOfOptionalsWithAutomaticUnwrapping.invalidArray() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"array[0].<iterable element>",
				"array[1].<iterable element>" );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "array" )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null ),
				pathWith()
						.property( "array" )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
		);
	}

	@Test
	public void validation_of_nested_type_arguments_works_on_getter_with_map_of_list_of_optional() {
		Set<ConstraintViolation<MapOfListsUsingGetter>> constraintViolations = validator.validate( MapOfListsUsingGetter.invalidStringFoo() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"map[key1].<map value>[0].<iterable element>",
				"map[key1].<map value>[1].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class, Size.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 ),
				pathWith()
						.property( "map" )
						.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, List.class, 0 )
		);
	}

	private static class MapOfLists {

		private Map<@Size(min = 2) String, @NotNull @Size(min = 2) List<Optional<@Size(min = 3) String>>> map;

		private static MapOfLists validFoo() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key", list );

			return foo;
		}

		private static MapOfLists invalidKeyFoo() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}

		private static MapOfLists invalidListFoo() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "only one value" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static MapOfLists invalidStringFoo() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ), Optional.of( "2" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static MapOfLists reallyInvalidFoo() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}
	}

	private static class MapOfListsUsingGetter {

		private Map<String, List<Optional<String>>> map;

		static MapOfListsUsingGetter invalidStringFoo() {
			MapOfListsUsingGetter mapOfListsUsingGetter = new MapOfListsUsingGetter();
			mapOfListsUsingGetter.map = MapOfLists.invalidStringFoo().map;
			return mapOfListsUsingGetter;
		}

		@SuppressWarnings("unused")
		Map<@Size(min = 2) String, @NotNull @Size(min = 2) List<Optional<@Size(min = 3) String>>> getMap() {
			return map;
		}
	}

	private static class MapOfListsWithAutomaticUnwrapping {

		private Map<@Size(min = 2) String, List<@NotNull(payload = { Unwrapping.Skip.class }) @Size(min = 2) StringProperty>> map;

		private static MapOfListsWithAutomaticUnwrapping validBar() {
			MapOfListsWithAutomaticUnwrapping bar = new MapOfListsWithAutomaticUnwrapping();

			List<StringProperty> list = Arrays.asList( new SimpleStringProperty( "one" ), new SimpleStringProperty( "tw" ),
					new SimpleStringProperty( "three" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}

		private static MapOfListsWithAutomaticUnwrapping invalidStringPropertyBar() {
			MapOfListsWithAutomaticUnwrapping bar = new MapOfListsWithAutomaticUnwrapping();

			List<StringProperty> list = Arrays.asList( new SimpleStringProperty( "one" ), new SimpleStringProperty( "t" ),
					new SimpleStringProperty( "three" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}

		private static MapOfListsWithAutomaticUnwrapping invalidListElementBar() {
			MapOfListsWithAutomaticUnwrapping bar = new MapOfListsWithAutomaticUnwrapping();

			List<StringProperty> list = Arrays.asList( null, new SimpleStringProperty( "two" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private static class ArrayOfOptionalsWithAutomaticUnwrapping {

		private Optional<@Size(min = 3) StringProperty> @NotNull [] array;

		private static ArrayOfOptionalsWithAutomaticUnwrapping validBaz() {
			ArrayOfOptionalsWithAutomaticUnwrapping baz = new ArrayOfOptionalsWithAutomaticUnwrapping();

			baz.array = new Optional[] { Optional.of( new SimpleStringProperty( "string1" ) ), Optional.of( new SimpleStringProperty( "string2" ) ) };

			return baz;
		}

		private static ArrayOfOptionalsWithAutomaticUnwrapping invalidArray() {
			ArrayOfOptionalsWithAutomaticUnwrapping baz = new ArrayOfOptionalsWithAutomaticUnwrapping();

			baz.array = new Optional[] { null, Optional.of( new SimpleStringProperty( "st" ) ) };

			return baz;
		}

	}

}
