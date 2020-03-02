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
import jakarta.validation.constraints.Null;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test usage of {@link Optional} on fields using validate value.
 *
 * @author George Gastaldi
 */
public class OptionalTypeAnnotationConstraintUsingValidateValueTest {
	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-1022")
	public void type_annotation_constraint_violation_is_propagated_even_if_there_are_no_other_constraints() {
		Model model = new Model();
		model.valueWithNullOrNotBlank = Optional.of( "" );

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateValue(
				Model.class,
				"valueWithNullOrNotBlank",
				model.valueWithNullOrNotBlank
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
						.withProperty( "valueWithNullOrNotBlank" )
						.withMessage( "type" )
		);
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
		Optional<@NullOrNotBlank(message = "type") String> valueWithNullOrNotBlank;
	}
}
