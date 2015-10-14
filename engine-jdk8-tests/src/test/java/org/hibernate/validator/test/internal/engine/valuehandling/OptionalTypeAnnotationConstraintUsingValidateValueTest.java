/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import org.fest.assertions.Assertions;
import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.constraints.NotBlankTypeUse;
import org.hibernate.validator.testutil.constraints.NotNullTypeUse;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.validation.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.Set;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.*;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Test usage of {@link Optional} on fields using validate value.
 *
 * @author George Gastaldi
 */
@TestForIssue(jiraKey = "HV-1022")
public class OptionalTypeAnnotationConstraintUsingValidateValueTest
{

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}
	@Test
	public void null_or_not_blank_type_on_optional_is_validated_for_blank_string() {
		Model model = new Model();
		model.valueWithNullOrNotBlank = Optional.of( "" );

		Set<ConstraintViolation<Model>> constraintViolations = validator.validateValue(
					Model.class,
					"valueWithNullOrNotBlank",
			                model.valueWithNullOrNotBlank
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "valueWithNullOrNotBlank" );
		assertCorrectConstraintViolationMessages( constraintViolations, "type" );
		assertCorrectConstraintTypes(constraintViolations, NullOrNotBlank.class );
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
