/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.valueextraction.Unwrapping;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test combination of {@link Optional} and {@code validateUnwrappedValue()} on getters.
 *
 * @author Davide D'Alto
 */
@TestForIssue(jiraKey = "HV-976")
public class OptionalTypeAnnotationConstraintOnGetterTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_null_value() {
		ModelA model = new ModelA();
		model.setValueWithoutTypeAnnotation( null );

		Set<ConstraintViolation<ModelA>> constraintViolations = validator.validate( model );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "container" )
						.withProperty( "valueWithoutTypeAnnotation" )
		);
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_empty_value() {
		ModelA model = new ModelA();
		model.setValueWithoutTypeAnnotation( Optional.empty() );

		Set<ConstraintViolation<ModelA>> constraintViolations = validator.validate( model );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_null_value() {
		ModelB model = new ModelB();
		model.setValueWithNotNull( null );

		Set<ConstraintViolation<ModelB>> constraintViolations = validator.validate( model );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "container" )
						.withProperty( "valueWithNotNull" )
		);
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_empty_value() {
		ModelB model = new ModelB();
		model.setValueWithNotNull( Optional.empty() );

		Set<ConstraintViolation<ModelB>> constraintViolations = validator.validate( model );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "type" )
						.withProperty( "valueWithNotNull" )
		);
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_null_value() {
		ModelD model = new ModelD();
		model.setValueWithNullOrNotBlank( null );

		Set<ConstraintViolation<ModelD>> constraintViolations = validator.validate( model );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "container" )
						.withProperty( "valueWithNullOrNotBlank" )
		);
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_empty_value() {
		ModelD model = new ModelD();
		model.setValueWithNullOrNotBlank( Optional.empty() );

		Set<ConstraintViolation<ModelD>> constraintViolations = validator.validate( model );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void reference_is_validated_for_null_value_and_unwrapped() {
		ModelC model = new ModelC();
		model.setValueWithNotNullUnwrapped( Optional.empty() );

		Set<ConstraintViolation<ModelC>> constraintViolations = validator.validate( model );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "container" )
						.withProperty( "valueWithNotNullUnwrapped" ),
				violationOf( NotBlank.class )
						.withMessage( "type" )
						.withProperty( "valueWithNotNullUnwrapped" )
		);
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_blank_string() {
		ModelD model = new ModelD();
		model.setValueWithNullOrNotBlank( Optional.of( "" ) );

		Set<ConstraintViolation<ModelD>> constraintViolations = validator.validate( model );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
						.withMessage( "type" )
						.withProperty( "valueWithNullOrNotBlank" )
		);
	}

	@Test
	public void reference_is_validated_for_null_value() {
		ModelE model = new ModelE();
		model.setValueReference( null );

		Set<ConstraintViolation<ModelE>> constraintViolations = validator.validate( model );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void reference_is_validated_for_empty_string() {
		ModelE model = new ModelE();
		model.setValueReference( "" );

		Set<ConstraintViolation<ModelE>> constraintViolations = validator.validate( model );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
						.withMessage( "reference" )
						.withProperty( "valueReference" )
		);
	}

	@Test
	public void reference_is_validated_for_valid_string() {
		ModelE model = new ModelE();
		model.setValueReference( "1" );

		Set<ConstraintViolation<ModelE>> constraintViolations = validator.validate( model );
		assertNoViolations( constraintViolations );
	}

	@ConstraintComposition(CompositionType.OR)
	@Null
	@NotBlank
	@ReportAsSingleViolation
	@Constraint(validatedBy = {})
	@Target({ TYPE_USE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface NullOrNotBlank {

		String message() default "NullOrNotBlank";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	private static class ModelA {

		private Optional<String> valueWithoutTypeAnnotation;

		@NotNull(message = "container")
		public Optional<String> getValueWithoutTypeAnnotation() {
			return valueWithoutTypeAnnotation;
		}

		public void setValueWithoutTypeAnnotation(Optional<String> valueWithoutTypeAnnotation) {
			this.valueWithoutTypeAnnotation = valueWithoutTypeAnnotation;
		}
	}

	private static class ModelB {

		private Optional<String> valueWithNotNull;

		@NotNull(message = "container")
		public Optional<@NotNull(message = "type") String> getValueWithNotNull() {
			return valueWithNotNull;
		}

		public void setValueWithNotNull(Optional<String> valueWithNotNull) {
			this.valueWithNotNull = valueWithNotNull;
		}
	}

	private static class ModelC {

		private Optional<String> valueWithNotNullUnwrapped;

		@NotNull(message = "container", payload = { Unwrapping.Unwrap.class })
		public Optional<@NotBlank(message = "type") String> getValueWithNotNullUnwrapped() {
			return valueWithNotNullUnwrapped;
		}

		public void setValueWithNotNullUnwrapped(Optional<String> valueWithNotNullUnwrapped) {
			this.valueWithNotNullUnwrapped = valueWithNotNullUnwrapped;
		}
	}

	private static class ModelD {

		private Optional<String> valueWithNullOrNotBlank;

		@NotNull(message = "container")
		public Optional<@NullOrNotBlank(message = "type") String> getValueWithNullOrNotBlank() {
			return valueWithNullOrNotBlank;
		}

		public void setValueWithNullOrNotBlank(Optional<String> valueWithNullOrNotBlank) {
			this.valueWithNullOrNotBlank = valueWithNullOrNotBlank;
		}
	}

	private static class ModelE {

		private String valueReference;

		@NullOrNotBlank(message = "reference")
		public String getValueReference() {
			return valueReference;
		}

		public void setValueReference(String valueReference) {
			this.valueReference = valueReference;
		}
	}
}
