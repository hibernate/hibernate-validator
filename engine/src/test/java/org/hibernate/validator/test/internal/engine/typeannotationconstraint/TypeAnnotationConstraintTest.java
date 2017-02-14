/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.typeannotationconstraint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.testutil.MessageLoggedAssertionLogger;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests Java 8 type use annotations.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class TypeAnnotationConstraintTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	// Validate value extractors are not called on null values

	@Test
	public void value_extractor_not_called_on_null_values() {
		Set<ConstraintViolation<ModelWithNullValue>> constraintViolations = validator.validate( new ModelWithNullValue() );

		assertCorrectPropertyPaths( constraintViolations, "nullValue" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	// List

	@Test
	public void field_constraint_provided_on_type_parameter_of_a_list_gets_validated() {
		TypeWithList1 l = new TypeWithList1();
		l.names = Arrays.asList( "First", "", null );

		Set<ConstraintViolation<TypeWithList1>> constraintViolations = validator.validate( l );

		assertCorrectPropertyPaths( constraintViolations, "names[1].<iterable element>", "names[2].<iterable element>", "names[2].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void constraint_provided_on_custom_bean_used_as_list_parameter_gets_validated() {
		TypeWithList3 l = new TypeWithList3();
		l.bars = Arrays.asList( new Bar( 2 ), null );
		Set<ConstraintViolation<TypeWithList3>> constraintViolations = validator.validate( l );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "bars[1].<iterable element>", "bars[0].number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotNull.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1165")
	public void constraints_specified_on_list_and_on_type_parameter_of_list_get_validated() {
		TypeWithList4 l = new TypeWithList4();
		l.names = Arrays.asList( "First", "", null );
		Set<ConstraintViolation<TypeWithList4>> constraintViolations = validator.validate( l );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "names[1].<iterable element>", "names[2].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class, NotBlank.class );

		l = new TypeWithList4();
		l.names = new ArrayList<>();
		constraintViolations = validator.validate( l );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "names" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
	}

	@Test
	public void getter_constraint_provided_on_type_parameter_of_a_list_gets_validated() {
		TypeWithList5 l = new TypeWithList5();
		l.strings = Arrays.asList( "", "First", null );

		Set<ConstraintViolation<TypeWithList5>> constraintViolations = validator.validate( l );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "strings[0].<iterable element>", "strings[2].<iterable element>", "strings[2].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void return_value_constraint_provided_on_type_parameter_of_a_list_gets_validated() throws Exception {
		Method method = TypeWithList6.class.getDeclaredMethod( "returnStrings" );
		Set<ConstraintViolation<TypeWithList6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithList6(),
				method,
				Arrays.asList( "First", "", null )
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"returnStrings.<return value>[1].<iterable element>",
				"returnStrings.<return value>[2].<iterable element>",
				"returnStrings.<return value>[2].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1121")
	public void property_path_contains_index_information_for_list() {
		TypeWithList1 l = new TypeWithList1();
		l.names = Arrays.asList( "" );

		Set<ConstraintViolation<TypeWithList1>> constraintViolations = validator.validate( l );

		assertNumberOfViolations( constraintViolations, 1 );

		Iterator<Path.Node> propertyPathIterator = constraintViolations.iterator().next().getPropertyPath().iterator();

		Path.Node firstNode = propertyPathIterator.next();
		assertThat( firstNode.getIndex() ).isNull();
		assertThat( firstNode.getName() ).isEqualTo( "names" );
		assertThat( firstNode.getKind() ).isEqualTo( ElementKind.PROPERTY );

		Path.Node secondNode = propertyPathIterator.next();
		assertThat( secondNode.getIndex() ).isEqualTo( 0 );
		assertThat( secondNode.getName() ).isEqualTo( NodeImpl.ITERABLE_ELEMENT_NODE_NAME );
		assertThat( secondNode.getKind() ).isEqualTo( ElementKind.PROPERTY );
	}

	@Test
	public void method_parameter_constraint_provided_as_type_parameter_of_a_list_gets_validated() throws Exception {
		Method method = TypeWithList7.class.getDeclaredMethod( "setValues", List.class );
		Object[] values = new Object[] { Arrays.asList( "", "First", null ) };

		Set<ConstraintViolation<TypeWithList7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithList7(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"setValues.listParameter[0].<iterable element>",
				"setValues.listParameter[2].<iterable element>",
				"setValues.listParameter[2].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void constructor_parameter_constraint_provided_on_type_parameter_of_a_list_gets_validated() throws Exception {
		Constructor<TypeWithList8> constructor = TypeWithList8.class.getDeclaredConstructor( List.class );
		Object[] values = new Object[] { Arrays.asList( "", "First", null ) };

		Set<ConstraintViolation<TypeWithList8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"TypeWithList8.listParameter[0].<iterable element>",
				"TypeWithList8.listParameter[2].<iterable element>",
				"TypeWithList8.listParameter[2].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	// Set

	@Test
	@TestForIssue(jiraKey = "HV-1165")
	public void field_constraint_provided_on_type_parameter_of_a_set_gets_validated() {
		TypeWithSet1 s = new TypeWithSet1();
		s.names = CollectionHelper.asSet( "First", "", null );

		Set<ConstraintViolation<TypeWithSet1>> constraintViolations = validator.validate( s );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "names[].<iterable element>", "names[].<iterable element>", "names[].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1165")
	public void constraint_provided_on_custom_bean_used_as_set_parameter_gets_validated() {
		TypeWithSet3 s = new TypeWithSet3();
		s.bars = CollectionHelper.asSet( new Bar( 2 ), null );
		Set<ConstraintViolation<TypeWithSet3>> constraintViolations = validator.validate( s );
		assertCorrectPropertyPaths( constraintViolations, "bars[].<iterable element>", "bars[].number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotNull.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1165")
	public void constraints_specified_on_set_and_on_type_parameter_of_set_get_validated() {
		TypeWithSet4 s = new TypeWithSet4();
		s.names = CollectionHelper.asSet( "First", "", null );
		Set<ConstraintViolation<TypeWithSet4>> constraintViolations = validator.validate( s );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "names[].<iterable element>", "names[].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class, NotBlank.class );

		s = new TypeWithSet4();
		s.names = new HashSet<>();
		constraintViolations = validator.validate( s );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "names" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1165")
	public void getter_constraint_provided_on_type_parameter_of_a_set_gets_validated() {
		TypeWithSet5 s = new TypeWithSet5();
		s.strings = new HashSet<>();
		s.strings.add( "First" );
		s.strings.add( "" );
		s.strings.add( null );

		Set<ConstraintViolation<TypeWithSet5>> constraintViolations = validator.validate( s );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "strings[].<iterable element>", "strings[].<iterable element>", "strings[].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1165")
	public void return_value_constraint_provided_on_type_parameter_of_a_set_gets_validated() throws Exception {
		Method method = TypeWithSet6.class.getDeclaredMethod( "returnStrings" );
		Set<ConstraintViolation<TypeWithSet6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithSet6(),
				method,
				CollectionHelper.asSet( "First", "", null )
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"returnStrings.<return value>[].<iterable element>",
				"returnStrings.<return value>[].<iterable element>",
				"returnStrings.<return value>[].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void method_parameter_constraint_provided_as_type_parameter_of_a_set_gets_validated() throws Exception {
		Method method = TypeWithSet7.class.getDeclaredMethod( "setValues", Set.class );
		Object[] values = new Object[] { CollectionHelper.asSet( "", "First", null ) };

		Set<ConstraintViolation<TypeWithSet7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithSet7(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"setValues.setParameter[].<iterable element>",
				"setValues.setParameter[].<iterable element>",
				"setValues.setParameter[].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void constructor_parameter_constraint_provided_on_type_parameter_of_a_set_gets_validated() throws Exception {
		Constructor<TypeWithSet8> constructor = TypeWithSet8.class.getDeclaredConstructor( Set.class );
		Object[] values = new Object[] { CollectionHelper.asSet( "", "First", null ) };

		Set<ConstraintViolation<TypeWithSet8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"TypeWithSet8.setParameter[].<iterable element>",
				"TypeWithSet8.setParameter[].<iterable element>",
				"TypeWithSet8.setParameter[].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	// Map

	@Test
	public void constraint_specified_on_value_type_of_map_gets_validated() {
		TypeWithMap1 m = new TypeWithMap1();
		m.nameMap = newHashMap();
		m.nameMap.put( "first", "Name 1" );
		m.nameMap.put( "second", "" );
		m.nameMap.put( "third", "Name 3" );
		Set<ConstraintViolation<TypeWithMap1>> constraintViolations = validator.validate( m );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "nameMap[second].<map value>" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class );
	}

	@Test
	public void constraint_provided_on_custom_bean_used_as_map_parameter_gets_validated() {
		TypeWithMap3 m = new TypeWithMap3();
		m.barMap = newHashMap();
		m.barMap.put( "bar", new Bar( 2 ) );
		m.barMap.put( "foo", null );
		Set<ConstraintViolation<TypeWithMap3>> constraintViolations = validator.validate( m );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "barMap[foo].<map value>", "barMap[bar].number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotNull.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1062")
	public void constraints_specified_on_map_and_on_value_type_of_map_get_validated() {
		TypeWithMap4 m = new TypeWithMap4();
		m.nameMap = newHashMap();
		m.nameMap.put( "first", "Name 1" );
		m.nameMap.put( "second", "" );
		m.nameMap.put( "third", "Name 3" );
		Set<ConstraintViolation<TypeWithMap4>> constraintViolations = validator.validate( m );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "nameMap[second].<map value>" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class );

		m = new TypeWithMap4();
		constraintViolations = validator.validate( m );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "nameMap" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void getter_constraint_provided_on_type_parameter_of_a_map_gets_validated() {
		TypeWithMap5 m = new TypeWithMap5();
		m.stringMap = newHashMap();
		m.stringMap.put( "first", "" );
		m.stringMap.put( "second", "Second" );
		m.stringMap.put( "third", null );

		Set<ConstraintViolation<TypeWithMap5>> constraintViolations = validator.validate( m );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "stringMap[first].<map value>", "stringMap[third].<map value>",
				 "stringMap[third].<map value>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void return_value_constraint_provided_on_type_parameter_of_a_map_gets_validated() throws Exception {
		Method method = TypeWithMap6.class.getDeclaredMethod( "returnStringMap" );

		Map<String, String> parameter = newHashMap();
		parameter.put( "first", "First" );
		parameter.put( "second", "" );
		parameter.put( "third", null );

		Set<ConstraintViolation<TypeWithMap6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithMap6(),
				method,
				parameter
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"returnStringMap.<return value>[second].<map value>",
				"returnStringMap.<return value>[third].<map value>",
				"returnStringMap.<return value>[third].<map value>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void property_path_contains_index_information_for_map() {
		TypeWithMap1 m = new TypeWithMap1();
		m.nameMap = newHashMap();
		m.nameMap.put( "first", "" );

		Set<ConstraintViolation<TypeWithMap1>> constraintViolations = validator.validate( m );

		assertNumberOfViolations( constraintViolations, 1 );

		Iterator<Path.Node> propertyPathIterator = constraintViolations.iterator().next().getPropertyPath().iterator();

		Path.Node firstNode = propertyPathIterator.next();
		assertThat( firstNode.getKey() ).isNull();
		assertThat( firstNode.getName() ).isEqualTo( "nameMap" );
		assertThat( firstNode.getKind() ).isEqualTo( ElementKind.PROPERTY );

		Path.Node secondNode = propertyPathIterator.next();
		assertThat( secondNode.getKey() ).isEqualTo( "first" );
		assertThat( secondNode.getName() ).isEqualTo( NodeImpl.MAP_VALUE_NODE_NAME );
		assertThat( secondNode.getKind() ).isEqualTo( ElementKind.PROPERTY );
	}

	@Test
	public void method_parameter_constraint_provided_as_type_parameter_of_a_map_gets_validated() throws Exception {
		Method method = TypeWithMap7.class.getDeclaredMethod( "setValues", Map.class );

		Map<String, String> parameter = newHashMap();
		parameter.put( "first", "First" );
		parameter.put( "second", "" );
		parameter.put( "third", null );
		Object[] values = new Object[] { parameter };

		Set<ConstraintViolation<TypeWithMap7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithMap7(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"setValues.mapParameter[second].<map value>",
				"setValues.mapParameter[third].<map value>",
				"setValues.mapParameter[third].<map value>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	public void constructor_parameter_constraint_provided_on_type_parameter_of_a_map_gets_validated() throws Exception {
		Constructor<TypeWithMap8> constructor = TypeWithMap8.class.getDeclaredConstructor( Map.class );

		Map<String, String> parameter = newHashMap();
		parameter.put( "first", "First" );
		parameter.put( "second", "" );
		parameter.put( "third", null );
		Object[] values = new Object[] { parameter };

		Set<ConstraintViolation<TypeWithMap8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"TypeWithMap8.mapParameter[second].<map value>",
				"TypeWithMap8.mapParameter[third].<map value>",
				"TypeWithMap8.mapParameter[third].<map value>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	// Array

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void field_constraint_provided_on_type_parameter_of_an_array_gets_validated() {
		TypeWithArray1 a = new TypeWithArray1();
		a.names = new String[]{ "First", "", null };

		Set<ConstraintViolation<TypeWithArray1>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "names[1].<iterable element>", "names[2].<iterable element>", "names[2].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void constraint_provided_on_custom_bean_used_as_array_parameter_gets_validated() {
		TypeWithArray3 a = new TypeWithArray3();
		a.bars = new Bar[]{ new Bar( 2 ), null };
		Set<ConstraintViolation<TypeWithArray3>> constraintViolations = validator.validate( a );
		assertCorrectPropertyPaths( constraintViolations, "bars[1].<iterable element>", "bars[0].number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotNull.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void constraints_specified_on_array_and_on_type_parameter_of_array_get_validated() {
		TypeWithArray4 a = new TypeWithArray4();
		a.names = new String[]{ "First", "", null };
		Set<ConstraintViolation<TypeWithArray4>> constraintViolations = validator.validate( a );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "names[1].<iterable element>", "names[2].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class, NotBlank.class );

		a = new TypeWithArray4();
		a.names = new String[0];
		constraintViolations = validator.validate( a );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "names" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void getter_constraint_provided_on_type_parameter_of_an_array_gets_validated() {
		TypeWithArray5 a = new TypeWithArray5();
		a.strings = new String[]{ "", "First", null };

		Set<ConstraintViolation<TypeWithArray5>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths( constraintViolations, "strings[0].<iterable element>", "strings[2].<iterable element>", "strings[2].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void return_value_constraint_provided_on_type_parameter_of_an_array_gets_validated() throws Exception {
		Method method = TypeWithArray6.class.getDeclaredMethod( "returnStrings" );
		Set<ConstraintViolation<TypeWithArray6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithArray6(),
				method,
				new String[]{ "First", "", null }
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"returnStrings.<return value>[1].<iterable element>",
				"returnStrings.<return value>[2].<iterable element>",
				"returnStrings.<return value>[2].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void property_path_contains_index_information_for_array() {
		TypeWithArray1 a = new TypeWithArray1();
		a.names = new String[]{ "" };

		Set<ConstraintViolation<TypeWithArray1>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 1 );

		Iterator<Path.Node> propertyPathIterator = constraintViolations.iterator().next().getPropertyPath().iterator();

		Path.Node firstNode = propertyPathIterator.next();
		assertThat( firstNode.getIndex() ).isNull();
		assertThat( firstNode.getName() ).isEqualTo( "names" );
		assertThat( firstNode.getKind() ).isEqualTo( ElementKind.PROPERTY );

		Path.Node secondNode = propertyPathIterator.next();
		assertThat( secondNode.getIndex() ).isEqualTo( 0 );
		assertThat( secondNode.getName() ).isEqualTo( NodeImpl.ITERABLE_ELEMENT_NODE_NAME );
		assertThat( secondNode.getKind() ).isEqualTo( ElementKind.PROPERTY );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void method_parameter_constraint_provided_as_type_parameter_of_an_array_gets_validated() throws Exception {
		Method method = TypeWithArray7.class.getDeclaredMethod( "setValues", String[].class );
		Object[] values = new Object[] { new String[]{ "", "First", null } };

		Set<ConstraintViolation<TypeWithArray7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithArray7(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"setValues.arrayParameter[0].<iterable element>",
				"setValues.arrayParameter[2].<iterable element>",
				"setValues.arrayParameter[2].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void constructor_parameter_constraint_provided_on_type_parameter_of_an_array_gets_validated() throws Exception {
		Constructor<TypeWithArray8> constructor = TypeWithArray8.class.getDeclaredConstructor( String[].class );
		Object[] values = new Object[] { new String[]{ "", "First", null } };

		Set<ConstraintViolation<TypeWithArray8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"TypeWithArray8.arrayParameter[0].<iterable element>",
				"TypeWithArray8.arrayParameter[2].<iterable element>",
				"TypeWithArray8.arrayParameter[2].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class,
				NotBlank.class,
				NotNull.class
		);
	}

	// Array of primitives

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void field_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated() {
		TypeWithArrayOfPrimitives1 a = new TypeWithArrayOfPrimitives1();
		a.ints = new int[]{ 6, 1 };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives1>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ints[1].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				Min.class
		);
	}

	// case 3 does not make sense here so we skip it

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void constraints_specified_on_array_and_on_type_parameter_of_array_of_primitives_get_validated() {
		TypeWithArrayOfPrimitives4 a = new TypeWithArrayOfPrimitives4();
		a.ints = new int[]{ 6, 1 };
		Set<ConstraintViolation<TypeWithArrayOfPrimitives4>> constraintViolations = validator.validate( a );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ints[1].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );

		a = new TypeWithArrayOfPrimitives4();
		a.ints = new int[0];
		constraintViolations = validator.validate( a );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ints" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void getter_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated() {
		TypeWithArrayOfPrimitives5 a = new TypeWithArrayOfPrimitives5();
		a.ints = new int[]{ 6, 1 };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives5>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ints[1].<iterable element>" );
		assertCorrectConstraintTypes(
				constraintViolations,
				Min.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void return_value_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated() throws Exception {
		Method method = TypeWithArrayOfPrimitives6.class.getDeclaredMethod( "returnInts" );
		Set<ConstraintViolation<TypeWithArrayOfPrimitives6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithArrayOfPrimitives6(),
				method,
				new int[]{ 6, 1 }
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"returnInts.<return value>[1].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				Min.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void property_path_contains_index_information_for_array_of_primitives() {
		TypeWithArrayOfPrimitives1 a = new TypeWithArrayOfPrimitives1();
		a.ints = new int[]{ 1 };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives1>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 1 );

		Iterator<Path.Node> propertyPathIterator = constraintViolations.iterator().next().getPropertyPath().iterator();

		Path.Node firstNode = propertyPathIterator.next();
		assertThat( firstNode.getIndex() ).isNull();
		assertThat( firstNode.getName() ).isEqualTo( "ints" );
		assertThat( firstNode.getKind() ).isEqualTo( ElementKind.PROPERTY );

		Path.Node secondNode = propertyPathIterator.next();
		assertThat( secondNode.getIndex() ).isEqualTo( 0 );
		assertThat( secondNode.getName() ).isEqualTo( NodeImpl.ITERABLE_ELEMENT_NODE_NAME );
		assertThat( secondNode.getKind() ).isEqualTo( ElementKind.PROPERTY );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void method_parameter_constraint_provided_as_type_parameter_of_an_array_of_primitives_gets_validated() throws Exception {
		Method method = TypeWithArrayOfPrimitives7.class.getDeclaredMethod( "setValues", int[].class );
		Object[] values = new Object[] { new int[]{ 6, 1 } };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithArrayOfPrimitives7(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"setValues.arrayParameter[1].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				Min.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1175")
	public void constructor_parameter_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated() throws Exception {
		Constructor<TypeWithArrayOfPrimitives8> constructor = TypeWithArrayOfPrimitives8.class.getDeclaredConstructor( int[].class );
		Object[] values = new Object[] { new int[]{ 6, 1 } };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"TypeWithArrayOfPrimitives8.arrayParameter[1].<iterable element>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				Min.class
		);
	}

	// Optional

	@Test
	public void constraint_specified_on_type_parameter_of_optional_gets_validated() {
		TypeWithOptional1 o = new TypeWithOptional1();
		o.stringOptional = Optional.of( "" );

		Set<ConstraintViolation<TypeWithOptional1>> constraintViolations = validator.validate( o );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "stringOptional" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class );
	}

	// Case 2 does not make sense here so we skip it

	@Test
	public void constraint_provided_on_custom_bean_used_as_optional_parameter_gets_validated() {
		TypeWithOptional3 o = new TypeWithOptional3();
		o.bar = Optional.empty();
		Set<ConstraintViolation<TypeWithOptional3>> constraintViolations = validator.validate( o );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "bar" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void constraints_specified_on_optional_and_on_type_parameter_of_optional_get_validated() {
		TypeWithOptional4 o = new TypeWithOptional4();
		o.stringOptional = Optional.of( "" );
		Set<ConstraintViolation<TypeWithOptional4>> constraintViolations = validator.validate( o );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "stringOptional" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class );

		o = new TypeWithOptional4();
		o.stringOptional = null;
		constraintViolations = validator.validate( o );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "stringOptional" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void getter_constraint_provided_on_type_parameter_of_an_optional_gets_validated() {
		TypeWithOptional5 o = new TypeWithOptional5();
		o.stringOptional = Optional.of( "" );

		Set<ConstraintViolation<TypeWithOptional5>> constraintViolations = validator.validate( o );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "stringOptional" );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class
		);
	}

	@Test
	public void return_value_constraint_provided_on_type_parameter_of_an_optional_gets_validated() throws Exception {
		Method method = TypeWithOptional6.class.getDeclaredMethod( "returnStringOptional" );
		Set<ConstraintViolation<TypeWithOptional6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithOptional6(),
				method,
				Optional.of( "" )
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"returnStringOptional.<return value>"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class
		);
	}

	@Test
	public void method_parameter_constraint_provided_as_type_parameter_of_an_optional_gets_validated()
			throws Exception {
		Method method = TypeWithOptional7.class.getDeclaredMethod( "setValues", Optional.class );
		Object[] values = new Object[] { Optional.of( "" ) };

		Set<ConstraintViolation<TypeWithOptional7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithOptional7(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"setValues.optionalParameter"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class
		);
	}

	@Test
	public void constructor_parameter_constraint_provided_on_type_parameter_of_an_optional_gets_validated()
			throws Exception {
		Constructor<TypeWithOptional8> constructor = TypeWithOptional8.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { Optional.of( "" ) };

		Set<ConstraintViolation<TypeWithOptional8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"TypeWithOptional8.optionalParameter"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotBlank.class
		);
	}

	// No unwrapper available

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000197.*")
	public void custom_generic_type_with_type_annotation_constraint_but_no_unwrapper_throws_exception() {
		// No unwrapper is registered for Baz
		BazHolder bazHolder = new BazHolder();
		bazHolder.baz = null;
		validator.validate( bazHolder );
	}

	@Test(enabled = false)
	// TODO decide what to do with this one; an error seems reasonable now, as there is no unwrapper/value extractor
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

	// Validate value extractors are not called on null values

	static class ModelWithNullValue {

		private @NotNull(message = "container") Optional<@NotNull(message = "type") String> nullValue;
	}

	// List

	static class TypeWithList1 {
		List<@NotNull @NotBlank String> names;
	}

	static class TypeWithList2 {
		List<@NotNull @NotBlank String> names;
	}

	static class TypeWithList3 {
		@Valid
		List<@NotNull Bar> bars;
	}

	static class TypeWithList4 {
		@Valid
		@Size(min = 1)
		List<@NotBlank String> names;
	}

	static class TypeWithList5 {
		List<String> strings;

		@Valid
		public List<@NotNull @NotBlank String> getStrings() {
			return strings;
		}
	}

	static class TypeWithList6 {
		List<String> strings;

		@Valid
		public List<@NotNull @NotBlank String> returnStrings() {
			return strings;
		}
	}

	static class TypeWithList7 {
		public void setValues(@Valid List<@NotNull @NotBlank String> listParameter) {
		}
	}

	static class TypeWithList8 {
		public TypeWithList8(@Valid List<@NotNull @NotBlank String> listParameter) {
		}
	}

	// Set

	static class TypeWithSet1 {
		@Valid
		Set<@NotNull @NotBlank String> names;
	}

	static class TypeWithSet2 {
		Set<@NotNull @NotBlank String> names;
	}

	static class TypeWithSet3 {
		@Valid
		Set<@NotNull Bar> bars;
	}

	static class TypeWithSet4 {
		@Valid
		@Size(min = 1)
		Set<@NotBlank String> names;
	}

	static class TypeWithSet5 {
		Set<String> strings;

		@Valid
		public Set<@NotNull @NotBlank String> getStrings() {
			return strings;
		}
	}

	static class TypeWithSet6 {
		Set<String> strings;

		@Valid
		public Set<@NotNull @NotBlank String> returnStrings() {
			return strings;
		}
	}

	static class TypeWithSet7 {
		public void setValues(@Valid Set<@NotNull @NotBlank String> setParameter) {
		}
	}

	static class TypeWithSet8 {
		public TypeWithSet8(@Valid Set<@NotNull @NotBlank String> setParameter) {
		}
	}

	// Map

	static class TypeWithMap1 {
		@Valid
		Map<String, @NotBlank String> nameMap;
	}

	static class TypeWithMap2 {
		Map<String, @NotNull @NotBlank String> nameMap;
	}

	static class TypeWithMap3 {
		@Valid
		Map<String, @NotNull Bar> barMap;
	}

	static class TypeWithMap4 {
		@Valid
		@NotNull
		Map<String, @NotBlank String> nameMap;
	}

	static class TypeWithMap5 {
		Map<String, String> stringMap;

		@Valid
		public Map<String, @NotNull @NotBlank String> getStringMap() {
			return stringMap;
		}
	}

	static class TypeWithMap6 {
		Map<String, String> stringMap;

		@Valid
		public Map<String, @NotNull @NotBlank String> returnStringMap() {
			return stringMap;
		}
	}

	static class TypeWithMap7 {
		public void setValues(@Valid Map<String, @NotNull @NotBlank String> mapParameter) {
		}
	}

	static class TypeWithMap8 {
		public TypeWithMap8(@Valid Map<String, @NotNull @NotBlank String> mapParameter) {
		}
	}

	// Array of objects

	static class TypeWithArray1 {
		@Valid
		String @NotNull @NotBlank [] names;
	}

	static class TypeWithArray2 {
		String @NotNull @NotBlank [] names;
	}

	static class TypeWithArray3 {
		@Valid
		Bar @NotNull [] bars;
	}

	static class TypeWithArray4 {
		@Valid
		@Size(min = 1)
		String @NotBlank [] names;
	}

	static class TypeWithArray5 {
		String[] strings;

		@Valid
		public String @NotNull @NotBlank [] getStrings() {
			return strings;
		}
	}

	static class TypeWithArray6 {
		String[] strings;

		@Valid
		public String @NotNull @NotBlank [] returnStrings() {
			return strings;
		}
	}

	static class TypeWithArray7 {
		public void setValues(@Valid String @NotNull @NotBlank [] arrayParameter) {
		}
	}

	static class TypeWithArray8 {
		public TypeWithArray8(@Valid String @NotNull @NotBlank [] arrayParameter) {
		}
	}

	// Array of primitives

	static class TypeWithArrayOfPrimitives1 {
		@Valid
		int @Min(4) [] ints;
	}

	static class TypeWithArrayOfPrimitives2 {
		int @Min(4) [] ints;
	}

	// case 3 does not make sense here so we skip it

	static class TypeWithArrayOfPrimitives4 {
		@Valid
		@Size(min = 2)
		int @Min(4) [] ints;
	}

	static class TypeWithArrayOfPrimitives5 {
		int[] ints;

		@Valid
		public int @Min(4) [] getInts() {
			return ints;
		}
	}

	static class TypeWithArrayOfPrimitives6 {
		int[] ints;

		@Valid
		public int @Min(4) [] returnInts() {
			return ints;
		}
	}

	static class TypeWithArrayOfPrimitives7 {
		public void setValues(@Valid int @Min(4) [] arrayParameter) {
		}
	}

	static class TypeWithArrayOfPrimitives8 {
		public TypeWithArrayOfPrimitives8(@Valid int @Min(4) [] arrayParameter) {
		}
	}

	// Optional

	static class TypeWithOptional1 {
		Optional<@NotBlank String> stringOptional;
	}

	static class TypeWithOptional3 {
		Optional<@NotNull Bar> bar;
	}

	static class TypeWithOptional4 {
		@NotNull
		Optional<@NotBlank String> stringOptional;
	}

	static class TypeWithOptional5 {
		Optional<String> stringOptional;

		public Optional<@NotNull @NotBlank String> getStringOptional() {
			return stringOptional;
		}
	}

	static class TypeWithOptional6 {
		Optional<String> stringOptional;

		public Optional<@NotNull @NotBlank String> returnStringOptional() {
			return stringOptional;
		}
	}

	static class TypeWithOptional7 {
		public void setValues(Optional<@NotBlank String> optionalParameter) {
		}
	}

	static class TypeWithOptional8 {
		public TypeWithOptional8(Optional<@NotBlank String> optionalParameter) {
		}
	}

	// No wrapper available

	static class Bar {
		@Min(4)
		Integer number;

		public Bar(Integer number) {
			this.number = number;
		}
	}

	static class BazHolder {
		Baz<@NotNull String> baz;
	}

	class Baz<T> {

	}

	class FooHolder {
		Foo<@NotNull Integer, @NotBlank String> foo;
	}

	class Foo<T, V> {

	}

}
