/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.constraints.UniqueElements;

import com.google.common.base.Objects;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link UniqueElements} constraint
 *
 * @author Tadhg Pearson
 */
public class UniqueElementsValidatorTest {

	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private static class AnnotationContainer {

		@UniqueElements
		private final List<Object> validateMe;

		AnnotationContainer(List<Object> validateMe) {
			this.validateMe = validateMe;
		}
	}

	@Test
	public void testValidDataPasses() {
		List<List<Object>> input = new ArrayList<>();
		input.add( null );
		input.add( Collections.singletonList( null ) );
		input.add( Collections.singletonList( "" ) );
		input.add( Collections.singletonList( "a" ) );
		input.add( Arrays.asList( "a", "aa" ) );
		input.add( Arrays.asList( "^", "ˆ" ) );
		input.add( Arrays.asList( "a", "b", "c", "1", "2", "3", null ) );
		input.add( Arrays.asList( "null", null ) );
		input.add( Arrays.asList( "lorem", "lorem ipsum" ) );

		input.add( Arrays.asList(
				new TestObject( 1 ),
				new TestObject( 2 ),
				new TestExtendedObject( 2 ),
				new TestExtendedObject( 3 )
		) );

		for ( List<Object> value : input ) {
			Set<ConstraintViolation<AnnotationContainer>> results = validator.validate( new AnnotationContainer( value ) );
			assertTrue( results.isEmpty(), "Validation should have passed for " + value + ", " + results );
		}
	}

	@Test
	public void testInvalidDataFails() {
		List<List<Object>> input = new ArrayList<>();
		input.add( Arrays.asList( null, null ) );
		input.add( Arrays.asList( "a", "a" ) );
		input.add( Arrays.asList( "ˆ", "ˆ" ) );
		input.add( Arrays.asList( "a", "b", "a" ) );
		input.add( Arrays.asList( "a", "b", "c", "b" ) );
		input.add( Arrays.asList( "*", "*" ) );

		input.add( Arrays.asList( new TestObject( 1 ), new TestObject( 2 ), new TestObject( 1 ) ) );
		input.add( Arrays.asList( new TestObject( 0 ), new TestObject( 0 ) ) );

		for ( List<Object> value : input ) {
			Set<ConstraintViolation<AnnotationContainer>> results = validator.validate( new AnnotationContainer( value ) );
			assertTrue( results.size() > 0, "Validation should have failed for " + value + ", " + results );
		}
	}

	@Test
	public void testMessageContainsDuplicatedValue() {
		String duplicate = "seeme";
		List<Object> fails = Arrays.asList( duplicate, duplicate );
		Set<ConstraintViolation<AnnotationContainer>> results = validator.validate( new AnnotationContainer( fails ) );
		assertTrue( results.stream().anyMatch( cv -> cv.getMessage().contains( duplicate ) ) );
	}

	private static class TestObject {

		final int value;

		TestObject(Integer value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( !( o instanceof TestObject ) ) {
				return false;
			}
			TestObject that = (TestObject) o;
			return value == that.value;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode( value );
		}
	}


	private static class TestExtendedObject extends TestObject {

		TestExtendedObject(Integer value) {
			super( value );
		}

		@Override
		public boolean equals(Object o) {
			return ( this == o ) || ( o instanceof TestExtendedObject && super.equals( o ) );
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash( super.hashCode() );
		}
	}
}
