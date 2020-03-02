/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.valueextraction.Unwrapping;

import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1237")
@SuppressWarnings("restriction")
@CandidateForTck
public class NestedTypeArgumentsValueExtractorTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void validation_of_nested_type_arguments_works_with_map_of_list_of_optional() {
		Set<ConstraintViolation<MapOfLists>> constraintViolations = validator.validate( MapOfLists.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( MapOfLists.invalidKey() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, "k", null, Map.class, 0 )
						)
		);

		constraintViolations = validator.validate( MapOfLists.invalidList() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
						)
		);

		constraintViolations = validator.validate( MapOfLists.invalidString() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 1, List.class, 0 )
						)
		);

		constraintViolations = validator.validate( MapOfLists.reallyInvalid() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, "k", null, Map.class, 0 )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "k", null, Map.class, 1 )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "k", null, Map.class, 1 )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						)
		);
	}

	@Test
	public void validation_of_nested_type_arguments_works_with_map_of_list_of_stringproperty() {
		Set<ConstraintViolation<MapOfListsWithAutomaticUnwrapping>> constraintViolations = validator.validate( MapOfListsWithAutomaticUnwrapping.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( MapOfListsWithAutomaticUnwrapping.invalidStringProperty() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key", null, Map.class, 1 )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 1, List.class, 0 )
						)
		);

		constraintViolations = validator.validate( MapOfListsWithAutomaticUnwrapping.invalidListElement() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key", null, Map.class, 1 )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void validation_of_nested_type_arguments_works_with_array_of_optional_of_stringproperty() {
		Set<ConstraintViolation<ArrayOfOptionalsWithAutomaticUnwrapping>> constraintViolations = validator.validate( ArrayOfOptionalsWithAutomaticUnwrapping.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( ArrayOfOptionalsWithAutomaticUnwrapping.invalidArray() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
						)
		);
	}

	@Test
	public void validation_of_nested_type_arguments_works_on_getter_with_map_of_list_of_optional() {
		Set<ConstraintViolation<MapOfListsUsingGetter>> constraintViolations = validator.validate( MapOfListsUsingGetter.invalidString() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "key1", null, Map.class, 1 )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 1, List.class, 0 )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void validation_of_nested_type_arguments_works_on_nested_arrays() {
		Set<ConstraintViolation<NestedArray>> constraintViolations = validator.validate( NestedArray.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( NestedArray.invalidArrayFirstDimension() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
						)
		);

		constraintViolations = validator.validate( NestedArray.invalidArraySecondDimension() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
						)
		);
	}

	private static class MapOfLists {

		private Map<@Size(min = 2) String, @NotNull @Size(min = 2) List<Optional<@Size(min = 3) String>>> map;

		private static MapOfLists valid() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key", list );

			return foo;
		}

		private static MapOfLists invalidKey() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "one" ), Optional.of( "two" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}

		private static MapOfLists invalidList() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "only one value" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static MapOfLists invalidString() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ), Optional.of( "2" ) );
			foo.map = new HashMap<>();
			foo.map.put( "key1", list );

			return foo;
		}

		private static MapOfLists reallyInvalid() {
			MapOfLists foo = new MapOfLists();

			List<Optional<String>> list = Arrays.asList( Optional.of( "1" ) );
			foo.map = new HashMap<>();
			foo.map.put( "k", list );

			return foo;
		}
	}

	private static class MapOfListsUsingGetter {

		private Map<String, List<Optional<String>>> map;

		static MapOfListsUsingGetter invalidString() {
			MapOfListsUsingGetter mapOfListsUsingGetter = new MapOfListsUsingGetter();
			mapOfListsUsingGetter.map = MapOfLists.invalidString().map;
			return mapOfListsUsingGetter;
		}

		@SuppressWarnings("unused")
		Map<@Size(min = 2) String, @NotNull @Size(min = 2) List<Optional<@Size(min = 3) String>>> getMap() {
			return map;
		}
	}

	private static class MapOfListsWithAutomaticUnwrapping {

		private Map<@Size(min = 2) String, List<@NotNull(payload = { Unwrapping.Skip.class }) @Size(min = 2) StringProperty>> map;

		private static MapOfListsWithAutomaticUnwrapping valid() {
			MapOfListsWithAutomaticUnwrapping bar = new MapOfListsWithAutomaticUnwrapping();

			List<StringProperty> list = Arrays.asList( new SimpleStringProperty( "one" ), new SimpleStringProperty( "tw" ),
					new SimpleStringProperty( "three" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}

		private static MapOfListsWithAutomaticUnwrapping invalidStringProperty() {
			MapOfListsWithAutomaticUnwrapping bar = new MapOfListsWithAutomaticUnwrapping();

			List<StringProperty> list = Arrays.asList( new SimpleStringProperty( "one" ), new SimpleStringProperty( "t" ),
					new SimpleStringProperty( "three" ) );
			bar.map = new HashMap<>();
			bar.map.put( "key", list );

			return bar;
		}

		private static MapOfListsWithAutomaticUnwrapping invalidListElement() {
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

		private static ArrayOfOptionalsWithAutomaticUnwrapping valid() {
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

	@SuppressWarnings({ "unused" })
	private static class NestedArray {

		private String @Size(min = 2) [] @Email [] array;

		private static NestedArray valid() {
			NestedArray baz = new NestedArray();

			baz.array = new String[][]{ { "email1@example.com", "email2@example.com" }, { "email3@example.com", "email4@example.com" } };

			return baz;
		}

		private static NestedArray invalidArrayFirstDimension() {
			NestedArray baz = new NestedArray();

			baz.array = new String[][]{ { "email1@example.com" }, { "email3@example.com", "email4@example.com" } };

			return baz;
		}

		private static NestedArray invalidArraySecondDimension() {
			NestedArray baz = new NestedArray();

			baz.array = new String[][]{ { "email1@example.com", "email2@example.com" }, { "email3@example.com", "not an email" } };

			return baz;
		}
	}
}
