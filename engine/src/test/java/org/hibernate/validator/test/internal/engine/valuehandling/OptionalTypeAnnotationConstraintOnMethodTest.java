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
import java.lang.reflect.Method;
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
 * Test combination of {@link Optional} and {@code validateUnwrappedValue()} on methods.
 *
 * @author Davide D'Alto
 */
@TestForIssue(jiraKey = "HV-976")
public class OptionalTypeAnnotationConstraintOnMethodTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_null_value() throws Exception {
		Method method = ModelA.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelA>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelA(), method, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "method.valueWithoutTypeAnnotation" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_empty_value() throws Exception {
		Method method = ModelA.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelA>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelA(), method, values );

		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_null_value() throws Exception {
		Method method = ModelB.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelB>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelB(), method, values );

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "method.valueWithNotNull", "method.valueWithNotNull" );
		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class, NotNull.class );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_empty_value() throws Exception {
		Method method = ModelB.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelB>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelB(), method, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "method.valueWithNotNull" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_null_value() throws Exception {
		Method method = ModelD.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelD>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelD(), method, values );


		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "method.valueWithNullOrNotBlank" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_empty_value() throws Exception {
		Method method = ModelD.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelD>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelD(), method, values );

		assertThat( constraintViolations ).isEmpty();
	}

	@Test
	public void reference_is_validated_for_null_value_and_unwrapped() throws Exception {
		Method method = ModelC.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelC>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelC(), method, values );

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "method.valueWithNotNullUnwrapped", "method.valueWithNotNullUnwrapped" );
		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_blank_string() throws Exception {
		Method method = ModelD.class.getDeclaredMethod( "method", Optional.class );
		Object[] values = new Object[] { Optional.of( "" ) };
		Set<ConstraintViolation<ModelD>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelD(), method, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "method.valueWithNullOrNotBlank" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_null_value() throws Exception {
		Method method = ModelE.class.getDeclaredMethod( "method", String.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelE>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelE(), method, values );

		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void reference_is_validated_for_empty_string() throws Exception {
		Method method = ModelE.class.getDeclaredMethod( "method", String.class );
		Object[] values = new Object[] { "" };
		Set<ConstraintViolation<ModelE>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelE(), method, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "method.valueReference" );
		assertCorrectConstraintViolationMessages( constraintViolations, "reference" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_valid_string() throws Exception {
		Method method = ModelE.class.getDeclaredMethod( "method", String.class );
		Object[] values = new Object[] { "1" };
		Set<ConstraintViolation<ModelE>> constraintViolations = validator
				.forExecutables()
				.validateParameters( new ModelE(), method, values );

		assertThat( constraintViolations ).isEmpty();
	}

	@Test
	public void wrapped_return_value_gets_validated() throws Exception {
		Method method = ModelF.class.getDeclaredMethod( "method" );
		Set<ConstraintViolation<ModelF>> constraintViolations = validator
				.forExecutables()
				.validateReturnValue( new ModelF(), method, Optional.of( "" ) );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "method.<return value>" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
	}

	@Test
	public void return_value_wrapper_gets_validated() throws Exception {
		Method method = ModelF.class.getDeclaredMethod( "method" );
		Set<ConstraintViolation<ModelF>> constraintViolations = validator
				.forExecutables()
				.validateReturnValue( new ModelF(), method, Optional.of( "" ) );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "method.<return value>" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
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

	static class ModelA {

		public void method(@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.NO) Optional<String> valueWithoutTypeAnnotation) {
		}
	}

	static class ModelB {

		public void method(@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.NO) Optional<@NotNull(message = "type") String> valueWithNotNull) {
		}
	}

	static class ModelC {

		public void method(@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.YES) Optional<@NotBlank(message = "type") String> valueWithNotNullUnwrapped) {
		}
	}

	static class ModelD {

		public void method(@NotNull(message = "container", validateUnwrappedValue = ValidateUnwrappedValue.NO) Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank) {
		}
	}

	static class ModelE {

		public void method(@NullOrNotBlank(message = "reference") String valueReference) {
		}
	}

	static class ModelF {

		public
		@NotNull(message = "container")
		Optional<@NullOrNotBlank(message = "type") String> method() {
			return null;
		}
	}
}
