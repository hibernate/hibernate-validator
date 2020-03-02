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
 * Test combination of {@link Optional} and {@link Unwrapping.Unwrap} on fields using validate property.
 *
 * @author Davide D'Alto
 */
@TestForIssue(jiraKey = "HV-976")
public class OptionalTypeAnnotationConstraintUsingValidatePropertyTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_null_value() {
		Model model = new Model();
		model.valueWithoutTypeAnnotation = null;

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithoutTypeAnnotation"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withProperty( "valueWithoutTypeAnnotation" )
						.withMessage( "container" )
		);
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_empty_value() {
		Model model = new Model();
		model.valueWithoutTypeAnnotation = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithoutTypeAnnotation"
		);
		assertNoViolations( constraintViolations );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_null_value() {
		Model model = new Model();
		model.valueWithNotNull = null;

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueWithNotNull" );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withProperty( "valueWithNotNull" )
						.withMessage( "container" )
		);
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_empty_value() {
		Model model = new Model();
		model.valueWithNotNull = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueWithNotNull" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withProperty( "valueWithNotNull" )
						.withMessage( "type" )
		);
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_null_value() {
		Model model = new Model();
		model.valueWithNullOrNotBlank = null;

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNullOrNotBlank"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withProperty( "valueWithNullOrNotBlank" )
						.withMessage( "container" )
		);
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_empty_value() {
		Model model = new Model();
		model.valueWithNullOrNotBlank = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNullOrNotBlank"
		);
		assertNoViolations( constraintViolations );
	}

	@Test
	public void reference_is_validated_for_null_value_and_unwrapped() {
		Model model = new Model();
		model.valueWithNotNullUnwrapped = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNotNullUnwrapped"
		);

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withProperty( "valueWithNotNullUnwrapped" )
						.withMessage( "type" ),
				violationOf( NotNull.class )
						.withProperty( "valueWithNotNullUnwrapped" )
						.withMessage( "container" )
		);
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_blank_string() {
		ModelWithSingleTypeAnnotationConstraint model = new ModelWithSingleTypeAnnotationConstraint();
		model.valueWithNullOrNotBlank = Optional.of( "" );

		Set<ConstraintViolation<ModelWithSingleTypeAnnotationConstraint>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNullOrNotBlank"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
						.withProperty( "valueWithNullOrNotBlank" )
						.withMessage( "type" )
		);
	}

	@Test
	public void reference_is_validated_for_null_value() {
		Model model = new Model();
		model.valueReference = null;

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueReference" );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void reference_is_validated_for_empty_string() {
		Model model = new Model();
		model.valueReference = "";

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueReference" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
						.withProperty( "valueReference" )
						.withMessage( "reference" )
		);
	}

	@Test
	public void reference_is_validated_for_valid_string() {
		Model model = new Model();
		model.valueReference = "1";

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueReference" );
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

	private static class Model {

		@NotNull(message = "container")
		Optional<String> valueWithoutTypeAnnotation;

		@NotNull(message = "container")
		Optional<@NotNull(message = "type") String> valueWithNotNull;

		@NotNull(message = "container", payload = { Unwrapping.Unwrap.class })
		Optional<@NotBlank(message = "type") String> valueWithNotNullUnwrapped;

		@NotNull(message = "container")
		Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank;

		@NullOrNotBlank(message = "reference")
		String valueReference;
	}

	private static class ModelWithSingleTypeAnnotationConstraint {
		Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank;
	}
}
