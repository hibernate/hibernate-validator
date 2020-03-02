/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validator;

import org.assertj.core.api.Assertions;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.UniqueElements;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * Tests the {@link UniqueElements} constraint
 *
 * @author Tadhg Pearson
 */
public class UniqueElementsValidatorTest {

	private static class AnnotationContainer {

		@UniqueElements
		private final List<Object> validateMe;

		private AnnotationContainer(List<Object> validateMe) {
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
			Set<ConstraintViolation<AnnotationContainer>> violations = ValidatorUtil.getValidator().validate( new AnnotationContainer( value ) );
			assertNoViolations( violations, "Validation should have passed for " + value );
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
			Set<ConstraintViolation<AnnotationContainer>> violations = ValidatorUtil.getValidator().validate( new AnnotationContainer( value ) );
			assertThat( violations )
					.describedAs( "Validation should have failed for " + value )
					.containsOnlyViolations( violationOf( UniqueElements.class ) );
		}
	}

	@Test
	public void testMessageContainsDuplicatedValue() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();

		MessageInterpolator messageInterpolator = new ResourceBundleMessageInterpolator(
				new AggregateResourceBundleLocator(
						Arrays.asList( "org/hibernate/validator/test/internal/constraintvalidators/hv/UniqueElementsMessages" ),
						configuration.getDefaultResourceBundleLocator(),
						getClass().getClassLoader()
				)
		);

		Validator validator = configuration
				.messageInterpolator( messageInterpolator )
				.buildValidatorFactory().getValidator();

		String duplicate = "seeme";
		List<Object> fails = Arrays.asList( duplicate, duplicate );
		Set<ConstraintViolation<AnnotationContainer>> violations = validator.validate( new AnnotationContainer( fails ) );

		assertThat( violations ).containsOnlyViolations( violationOf( UniqueElements.class ) );

		assertTrue( violations.stream().anyMatch( cv -> cv.getMessage().contains( duplicate ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDymanicPayloadContainsDuplicatedValue() {
		String duplicate = "seeme";
		List<Object> fails = Arrays.asList( duplicate, duplicate );
		Set<ConstraintViolation<AnnotationContainer>> violations = ValidatorUtil.getValidator().validate( new AnnotationContainer( fails ) );

		assertThat( violations ).containsOnlyViolations( violationOf( UniqueElements.class ) );

		ConstraintViolation<?> violation = violations.iterator().next();
		Assertions.assertThat( ((HibernateConstraintViolation<UniqueElements>) violation.unwrap( HibernateConstraintViolation.class )).getDynamicPayload( List.class ) )
				.containsOnly( duplicate );
	}

	private static class TestObject {

		private final int value;

		private TestObject(Integer value) {
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
			return Objects.hash( value );
		}
	}


	private static class TestExtendedObject extends TestObject {

		private TestExtendedObject(Integer value) {
			super( value );
		}

		@Override
		public boolean equals(Object o) {
			return ( this == o ) || ( o instanceof TestExtendedObject && super.equals( o ) );
		}

		@Override
		public int hashCode() {
			return Objects.hash( super.hashCode() );
		}
	}
}
