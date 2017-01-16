/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.valueextraction.ValidateUnwrappedValue;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test combination of {@link Optional} and {@link UnwrapValidatedValue} on fields using validate property.
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
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithoutTypeAnnotation" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_empty_value() {
		Model model = new Model();
		model.valueWithoutTypeAnnotation = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithoutTypeAnnotation"
		);
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_null_value() {
		Model model = new Model();
		model.valueWithNotNull = null;

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueWithNotNull" );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNotNull", "valueWithNotNull" );
		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class, NotNull.class );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_empty_value() {
		Model model = new Model();
		model.valueWithNotNull = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueWithNotNull" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNotNull" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_null_value() {
		Model model = new Model();
		model.valueWithNullOrNotBlank = null;

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNullOrNotBlank"
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNullOrNotBlank" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_empty_value() {
		Model model = new Model();
		model.valueWithNullOrNotBlank = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNullOrNotBlank"
		);
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void reference_is_validated_for_null_value_and_unwrapped() {
		Model model = new Model();
		model.valueWithNotNullUnwrapped = Optional.empty();

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNotNullUnwrapped"
		);
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNotNullUnwrapped", "valueWithNotNullUnwrapped" );
		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_blank_string() {
		ModelWithSingleTypeAnnotationConstraint model = new ModelWithSingleTypeAnnotationConstraint();
		model.valueWithNullOrNotBlank = Optional.of( "" );

		Set<ConstraintViolation<ModelWithSingleTypeAnnotationConstraint>> constraintViolations = validator.validateProperty(
				model,
				"valueWithNullOrNotBlank"
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNullOrNotBlank" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_null_value() {
		Model model = new Model();
		model.valueReference = null;

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueReference" );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void reference_is_validated_for_empty_string() {
		Model model = new Model();
		model.valueReference = "";

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueReference" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueReference" );
		assertCorrectConstraintViolationMessages( constraintViolations, "reference" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_valid_string() {
		Model model = new Model();
		model.valueReference = "1";

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateProperty( model, "valueReference" );
		assertNumberOfViolations( constraintViolations, 0 );
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

		@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.NO)
		Optional<String> valueWithoutTypeAnnotation;

		@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.NO)
		Optional<@NotNull(message = "type") String> valueWithNotNull;

		@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.YES)
		Optional<@NotBlank(message = "type") String> valueWithNotNullUnwrapped;

		@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.NO)
		Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank;

		@NullOrNotBlank(message = "reference")
		String valueReference;
	}

	private static class ModelWithSingleTypeAnnotationConstraint {
		Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank;
	}
}
