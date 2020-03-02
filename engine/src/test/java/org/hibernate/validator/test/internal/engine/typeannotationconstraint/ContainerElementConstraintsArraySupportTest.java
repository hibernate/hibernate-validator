/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.typeannotationconstraint;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.engine.path.NodeImpl;
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
public class ContainerElementConstraintsArraySupportTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	// Array

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void field_constraint_provided_on_type_parameter_of_an_array_gets_validated() {
		TypeWithArray1 a = new TypeWithArray1();
		a.names = new String[] { "First", "", null };

		Set<ConstraintViolation<TypeWithArray1>> constraintViolations = validator.validate( a );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "names" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
						),
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "names" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						),
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "names" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void constraint_provided_on_custom_bean_used_as_array_parameter_gets_validated() {
		TypeWithArray3 a = new TypeWithArray3();
		a.bars = new Bar[] { new Bar( 2 ), null };
		Set<ConstraintViolation<TypeWithArray3>> constraintViolations = validator.validate( a );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "bars" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
						),
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "bars" )
								.property( "number", true, null, 0, Object[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void constraints_specified_on_array_and_on_type_parameter_of_array_get_validated() {
		TypeWithArray4 a = new TypeWithArray4();
		a.names = new String[] { "First", "", null };
		Set<ConstraintViolation<TypeWithArray4>> constraintViolations = validator.validate( a );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "names" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
						),
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "names" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
		);

		a = new TypeWithArray4();
		a.names = new String[0];
		constraintViolations = validator.validate( a );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "names" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void getter_constraint_provided_on_type_parameter_of_an_array_gets_validated() {
		TypeWithArray5 a = new TypeWithArray5();
		a.strings = new String[] { "", "First", null };

		Set<ConstraintViolation<TypeWithArray5>> constraintViolations = validator.validate( a );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "strings" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
						),
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "strings" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
				,
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "strings" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void return_value_constraint_provided_on_type_parameter_of_an_array_gets_validated() throws Exception {
		Method method = TypeWithArray6.class.getDeclaredMethod( "returnStrings" );
		Set<ConstraintViolation<TypeWithArray6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithArray6(),
				method,
				new String[] { "First", "", null }
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.method( "returnStrings" )
								.returnValue()
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, Object[].class, null )
						),
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.method( "returnStrings" )
								.returnValue()
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
				,
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.method( "returnStrings" )
								.returnValue()
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void property_path_contains_index_information_for_array() {
		TypeWithArray1 a = new TypeWithArray1();
		a.names = new String[] { "" };

		Set<ConstraintViolation<TypeWithArray1>> constraintViolations = validator.validate( a );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "names" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void method_parameter_constraint_provided_as_type_parameter_of_an_array_gets_validated() throws Exception {
		Method method = TypeWithArray7.class.getDeclaredMethod( "setValues", String[].class );
		Object[] values = new Object[] { new String[] { "", "First", null } };

		Set<ConstraintViolation<TypeWithArray7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithArray7(),
				method,
				values
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.method( "setValues" )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
						),
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.method( "setValues" )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
				,
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.method( "setValues" )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void constructor_parameter_constraint_provided_on_type_parameter_of_an_array_gets_validated()
			throws Exception {
		Constructor<TypeWithArray8> constructor = TypeWithArray8.class.getDeclaredConstructor( String[].class );
		Object[] values = new Object[] { new String[] { "", "First", null } };

		Set<ConstraintViolation<TypeWithArray8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.constructor( TypeWithArray8.class )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
						),
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.constructor( TypeWithArray8.class )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
				,
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.constructor( TypeWithArray8.class )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 2, Object[].class, null )
						)
		);
	}

	// Array of primitives

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void field_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated() {
		TypeWithArrayOfPrimitives1 a = new TypeWithArrayOfPrimitives1();
		a.ints = new int[] { 6, 1 };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives1>> constraintViolations = validator.validate( a );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "ints" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, int[].class, null )
						)
		);
	}

	// case 3 does not make sense here so we skip it

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void constraints_specified_on_array_and_on_type_parameter_of_array_of_primitives_get_validated() {
		TypeWithArrayOfPrimitives4 a = new TypeWithArrayOfPrimitives4();
		a.ints = new int[] { 6, 1 };
		Set<ConstraintViolation<TypeWithArrayOfPrimitives4>> constraintViolations = validator.validate( a );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "ints" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, int[].class, null )
						)
		);

		a = new TypeWithArrayOfPrimitives4();
		a.ints = new int[0];
		constraintViolations = validator.validate( a );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "ints" )
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void getter_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated() {
		TypeWithArrayOfPrimitives5 a = new TypeWithArrayOfPrimitives5();
		a.ints = new int[] { 6, 1 };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives5>> constraintViolations = validator.validate( a );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "ints" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, int[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void return_value_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated()
			throws Exception {
		Method method = TypeWithArrayOfPrimitives6.class.getDeclaredMethod( "returnInts" );
		Set<ConstraintViolation<TypeWithArrayOfPrimitives6>> constraintViolations = validator.forExecutables().validateReturnValue(
				new TypeWithArrayOfPrimitives6(),
				method,
				new int[] { 6, 1 }
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.method( "returnInts" )
								.returnValue()
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, int[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void property_path_contains_index_information_for_array_of_primitives() {
		TypeWithArrayOfPrimitives1 a = new TypeWithArrayOfPrimitives1();
		a.ints = new int[] { 1 };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives1>> constraintViolations = validator.validate( a );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class ).withPropertyPath( pathWith()
						.property( "ints" )
						.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, int[].class, null )
				)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void method_parameter_constraint_provided_as_type_parameter_of_an_array_of_primitives_gets_validated()
			throws Exception {
		Method method = TypeWithArrayOfPrimitives7.class.getDeclaredMethod( "setValues", int[].class );
		Object[] values = new Object[] { new int[] { 6, 1 } };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives7>> constraintViolations = validator.forExecutables().validateParameters(
				new TypeWithArrayOfPrimitives7(),
				method,
				values
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.method( "setValues" )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, int[].class, null )
						)
		);
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	@TestForIssue(jiraKey = "HV-1175")
	public void constructor_parameter_constraint_provided_on_type_parameter_of_an_array_of_primitives_gets_validated()
			throws Exception {
		Constructor<TypeWithArrayOfPrimitives8> constructor = TypeWithArrayOfPrimitives8.class.getDeclaredConstructor( int[].class );
		Object[] values = new Object[] { new int[] { 6, 1 } };

		Set<ConstraintViolation<TypeWithArrayOfPrimitives8>> constraintViolations = validator.forExecutables().validateConstructorParameters(
				constructor,
				values
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.constructor( TypeWithArrayOfPrimitives8.class )
								.parameter( "arrayParameter", 0 )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 1, int[].class, null )
						)
		);
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
		public void setValues(int @Min(4) [] arrayParameter) {
		}
	}

	static class TypeWithArrayOfPrimitives8 {
		public TypeWithArrayOfPrimitives8(int @Min(4) [] arrayParameter) {
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
}
