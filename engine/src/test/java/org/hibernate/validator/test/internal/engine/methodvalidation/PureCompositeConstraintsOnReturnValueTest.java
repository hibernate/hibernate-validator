/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@SuppressWarnings("deprecation")
public class PureCompositeConstraintsOnReturnValueTest {
	private Validator validator;
	private Foo foo;

	@BeforeMethod
	public void setUp() throws Exception {
		validator = ValidatorUtil.getValidator();
		foo = new Foo( "" );
	}

	@Test
	@TestForIssue( jiraKey = "HV-1494")
	public void testHVSpecificNotEmpty() throws Exception {
		Set<ConstraintViolation<Foo>> violations = validator.forExecutables()
				.validateReturnValue(
						foo,
						Foo.class.getDeclaredMethod( "createBarString", String.class ),
						""
				);
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotEmpty.class )
		);

		violations = validator.forExecutables()
				.validateReturnValue(
						foo,
						Foo.class.getDeclaredMethod( "createBarString", String.class ),
						" "
				);
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue( jiraKey = "HV-1494")
	public void testCustomComposingConstraintOnReturnValue() throws Exception {
		Set<ConstraintViolation<Foo>> violations = validator.forExecutables()
				.validateReturnValue(
						foo,
						Foo.class.getDeclaredMethod( "createCustomBarString", String.class ),
						"a"
				);
		assertThat( violations ).containsOnlyViolations(
				violationOf( CustomCompositeConstraint.class )
						.withPropertyPath( pathWith()
								.method( "createCustomBarString" )
								.returnValue()
						)
		);

		violations = validator.forExecutables()
				.validateReturnValue(
						foo,
						Foo.class.getDeclaredMethod( "createCustomBarString", String.class ),
						"1"
				);
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue( jiraKey = "HV-1494")
	public void testCustomComposingConstraintOnParameters() throws Exception {
		Set<ConstraintViolation<Foo>> violations = validator.forExecutables()
				.validateParameters(
						foo,
						Foo.class.getDeclaredMethod( "createCustomBarString", String.class ),
						new String[] { "abc" }
				);
		assertThat( violations ).containsOnlyViolations(
				violationOf( CustomCompositeConstraint.class )
						.withPropertyPath( pathWith()
								.method( "createCustomBarString" )
								.parameter( "a", 0 )
						)
		);

		violations = validator.forExecutables()
				.validateParameters(
						foo,
						Foo.class.getDeclaredMethod( "createCustomBarString", String.class ),
						new String[] { "1" }
				);
		assertNoViolations( violations );
	}

	private static class Foo {

		private String bar;

		public Foo(String bar) {
			this.bar = bar;
		}

		@NotEmpty
		public String createBarString(String a) {
			return bar;
		}

		@CustomCompositeConstraint
		public String createCustomBarString(@CustomCompositeConstraint String a) {
			return bar;
		}
	}

	@Documented
	@Constraint(validatedBy = { })
	@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@ReportAsSingleViolation
	@NotNull
	@Size(min = 1)
	@Pattern(regexp = "\\d*")
	public @interface CustomCompositeConstraint {
		String message() default "no message";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}
}
