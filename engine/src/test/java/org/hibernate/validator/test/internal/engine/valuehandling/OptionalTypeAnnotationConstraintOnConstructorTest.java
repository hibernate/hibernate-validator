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
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test combination of {@link Optional} and {@link UnwrapValidatedValue} on methods.
 *
 * @author Davide D'Alto
 */
@TestForIssue(jiraKey = "HV-976")
public class OptionalTypeAnnotationConstraintOnConstructorTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_null_value() throws Exception {
		Constructor<ModelA> constructor = ModelA.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelA>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ModelA.arg0" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void without_type_annotation_on_optional_is_validated_for_empty_value() throws Exception {
		Constructor<ModelA> constructor = ModelA.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelA>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_null_value() throws Exception {
		Constructor<ModelB> constructor = ModelB.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelB>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "ModelB.arg0", "ModelB.arg0" );

		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class, NotNull.class );
	}

	@Test
	public void not_null_type_on_optional_is_validated_for_empty_value() throws Exception {
		Constructor<ModelB> constructor = ModelB.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelB>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ModelB.arg0" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_null_value() throws Exception {
		Constructor<ModelD> constructor = ModelD.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelD>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );


		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ModelD.arg0" );
		assertCorrectConstraintViolationMessages( constraintViolations, "container" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_empty_value() throws Exception {
		Constructor<ModelD> constructor = ModelD.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelD>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertThat( constraintViolations ).isEmpty();
	}

	@Test
	public void reference_is_validated_for_null_value_and_unwrapped() throws Exception {
		Constructor<ModelC> constructor = ModelC.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { Optional.empty() };
		Set<ConstraintViolation<ModelC>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "ModelC.arg0", "ModelC.arg0" );
		assertThat( constraintViolations ).extracting( "message" ).containsOnly( "container", "type" );
		assertCorrectConstraintTypes( constraintViolations, NotBlank.class, NotNull.class );
	}

	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_blank_string() throws Exception {
		Constructor<ModelD> constructor = ModelD.class.getDeclaredConstructor( Optional.class );
		Object[] values = new Object[] { Optional.of( "" ) };
		Set<ConstraintViolation<ModelD>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ModelD.arg0" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_null_value() throws Exception {
		Constructor<ModelE> constructor = ModelE.class.getDeclaredConstructor( String.class );
		Object[] values = new Object[] { null };
		Set<ConstraintViolation<ModelE>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void reference_is_validated_for_empty_string() throws Exception {
		Constructor<ModelE> constructor = ModelE.class.getDeclaredConstructor( String.class );
		Object[] values = new Object[] { "" };
		Set<ConstraintViolation<ModelE>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "ModelE.arg0" );
		assertCorrectConstraintViolationMessages( constraintViolations, "reference" );
		assertCorrectConstraintTypes( constraintViolations, NullOrNotBlank.class );
	}

	@Test
	public void reference_is_validated_for_valid_string() throws Exception {
		Constructor<ModelE> constructor = ModelE.class.getDeclaredConstructor( String.class );
		Object[] values = new Object[] { "1" };
		Set<ConstraintViolation<ModelE>> constraintViolations = validator
				.forExecutables()
				.validateConstructorParameters( constructor, values );

		assertThat( constraintViolations ).isEmpty();
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

		public ModelA(@UnwrapValidatedValue(false) @NotNull(message = "container") Optional<String> valueWithoutTypeAnnotation) {
		}
	}

	static class ModelB {

		public ModelB(@UnwrapValidatedValue(false) @NotNull(message = "container") Optional<@NotNull(message = "type") String> valueWithNotNull) {
		}
	}

	static class ModelC {

		public ModelC(@UnwrapValidatedValue(true) @NotNull(message = "container") Optional<@NotBlank(message = "type") String> valueWithNotNullUnwrapped) {
		}
	}

	static class ModelD {

		public ModelD(@UnwrapValidatedValue(false) @NotNull(message = "container") Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank) {
		}
	}

	static class ModelE {

		public ModelE(@NullOrNotBlank(message = "reference") String valueReference) {
		}
	}
}
