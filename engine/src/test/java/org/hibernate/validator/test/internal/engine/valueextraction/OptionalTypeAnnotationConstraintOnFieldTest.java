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
 * Test combination of {@link Optional} and {@code validateUnwrappedValue()} on fields.
 *
 * @author Davide D'Alto
 */
@TestForIssue(jiraKey = "HV-976")
public class OptionalTypeAnnotationConstraintOnFieldTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_null_value() {
		ModelA model = new ModelA();
		model.valueWithoutTypeAnnotation = null;

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
		model.valueWithoutTypeAnnotation = Optional.empty();

		Set<ConstraintViolation<ModelA>> constraintViolations = validator.validate( model );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_null_value() {
		ModelB model = new ModelB();
		model.valueWithNotNull = null;

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
		model.valueWithNotNull = Optional.empty();

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
		model.valueWithNullOrNotBlank = null;

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
		model.valueWithNullOrNotBlank = Optional.empty();

		Set<ConstraintViolation<ModelD>> constraintViolations = validator.validate( model );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void reference_is_validated_for_null_value_and_unwrapped() {
		ModelC model = new ModelC();
		model.valueWithNotNullUnwrapped = Optional.empty();

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
		model.valueWithNullOrNotBlank = Optional.of( "" );

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
		model.valueReference = null;

		Set<ConstraintViolation<ModelE>> constraintViolations = validator.validate( model );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void reference_is_validated_for_empty_string() {
		ModelE model = new ModelE();
		model.valueReference = "";

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
		model.valueReference = "1";

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

		@NotNull(message = "container")
		Optional<String> valueWithoutTypeAnnotation;

	}

	private static class ModelB {

		@NotNull(message = "container")
		Optional<@NotNull(message = "type") String> valueWithNotNull;

	}

	private static class ModelC {

		@NotNull(message = "container", payload = { Unwrapping.Unwrap.class })
		Optional<@NotBlank(message = "type") String> valueWithNotNullUnwrapped;
	}

	private static class ModelD {

		@NotNull(message = "container")
		Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank;
	}

	private static class ModelE {

		@NullOrNotBlank(message = "reference")
		String valueReference;
	}
}
