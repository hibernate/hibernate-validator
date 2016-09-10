/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.constraints.NotBlankTypeUse;
import org.hibernate.validator.testutils.constraints.NotNullTypeUse;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

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

/**
 * Test combination of {@link Optional} and {@link UnwrapValidatedValue} on fields.
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
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithoutTypeAnnotation" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_empty_value() {
		ModelA model = new ModelA();
		model.valueWithoutTypeAnnotation = Optional.empty();

		Set<ConstraintViolation<ModelA>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_null_value() {
		ModelB model = new ModelB();
		model.valueWithNotNull = null;

		Set<ConstraintViolation<ModelB>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNotNull", "valueWithNotNull" );
		// TODO: We don't need to validate the type since the container already failed
		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class, NotNullTypeUse.class );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_empty_value() {
		ModelB model = new ModelB();
		model.valueWithNotNull = Optional.empty();

		Set<ConstraintViolation<ModelB>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNotNull" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNullTypeUse.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_null_value() {
		ModelD model = new ModelD();
		model.valueWithNullOrNotBlank = null;

		Set<ConstraintViolation<ModelD>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNullOrNotBlank" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_empty_value() {
		ModelD model = new ModelD();
		model.valueWithNullOrNotBlank = Optional.empty();

		Set<ConstraintViolation<ModelD>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void reference_is_validated_for_null_value_and_unwrapped() {
		ModelC model = new ModelC();
		model.valueWithNotNullUnwrapped = Optional.empty();

		Set<ConstraintViolation<ModelC>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNotNullUnwrapped", "valueWithNotNullUnwrapped" );
		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotBlankTypeUse.class, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_blank_string() {
		ModelD model = new ModelD();
		model.valueWithNullOrNotBlank = Optional.of( "" );

		Set<ConstraintViolation<ModelD>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNullOrNotBlank" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_null_value() {
		ModelE model = new ModelE();
		model.valueReference = null;

		Set<ConstraintViolation<ModelE>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void reference_is_validated_for_empty_string() {
		ModelE model = new ModelE();
		model.valueReference = "";

		Set<ConstraintViolation<ModelE>> constraintViolations = validator.validate( model );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueReference" );
		assertCorrectConstraintViolationMessages( constraintViolations, "reference" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_valid_string() {
		ModelE model = new ModelE();
		model.valueReference = "1";

		Set<ConstraintViolation<ModelE>> constraintViolations = validator.validate( model );
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

	private static class ModelA {

		@UnwrapValidatedValue(false)
		@NotNull(message = "container")
		Optional<String> valueWithoutTypeAnnotation;

	}

	private static class ModelB {

		@UnwrapValidatedValue(false)
		@NotNull(message = "container")
		Optional<@NotNullTypeUse(message = "type") String> valueWithNotNull;

	}

	private static class ModelC {

		@UnwrapValidatedValue(true)
		@NotNull(message = "container")
		Optional<@NotBlankTypeUse(message = "type") String> valueWithNotNullUnwrapped;
	}

	private static class ModelD {

		@UnwrapValidatedValue(false)
		@NotNull(message = "container")
		Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank;
	}

	private static class ModelE {

		@NullOrNotBlank(message = "reference")
		String valueReference;
	}
}
