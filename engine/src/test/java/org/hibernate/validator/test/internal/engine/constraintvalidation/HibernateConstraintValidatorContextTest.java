/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.junit.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * @author Hardy Ferentschik
 */
public class HibernateConstraintValidatorContextTest {

	private static final String QUESTION_1 = "The answer to life?";
	private static final String QUESTION_2 = "What is 1+1 and what is the answer to life?";
	private static final String QUESTION_3 = "This is a trick question";
	private static final String QUESTION_4 = "What keywords are not allowed?";

	private static final List<String> INVALID_KEYWORDS = Lists.newArrayList("foo", "bar", "baz");

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testSettingCustomMessageParameter() {
		Validator validator = getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( QUESTION_1 ) );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages( constraintViolations, "the answer is: 42" );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000028.*")
	@TestForIssue(jiraKey = "HV-701")
	public void testSettingInvalidCustomMessageParameter() {
		Validator validator = getValidator();
		validator.validate( new Foo( "" ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testCreatingMultipleConstraintViolation() {
		Validator validator = getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( QUESTION_2 ) );

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintViolationMessages( constraintViolations, "answer 1: 2", "answer 2: 42" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testExpressionTermAsAttributeValueIsTreatedAsString() {
		Validator validator = getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( QUESTION_3 ) );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages( constraintViolations, "the answer is: ${foo}" );
	}

	@Test
	@TestForIssue( jiraKey = "HV-951")
	public void testExpressionVariablesAreExposedInConstraintViolation() throws Exception {
		Validator validator = getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( QUESTION_1 ) );

		assertNumberOfViolations( constraintViolations, 1 );

		ConstraintViolationImpl<Foo> constraintViolation = (ConstraintViolationImpl<Foo>) constraintViolations.iterator()
				.next();
		Map<String, Object> expressionVariables = constraintViolation.getExpressionVariables();
		Assert.assertEquals( 1, expressionVariables.size() );
		Assert.assertEquals( 42, expressionVariables.get( "answer" ) );
	}

	@Test
	@TestForIssue( jiraKey = "BVAL-490")
	public void testInfoExposedInHibernateConstraintViolation() {
		Validator validator = getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( QUESTION_4 ) );

		assertNumberOfViolations( constraintViolations, 1 );

		ConstraintViolation<Foo> constraintViolation = constraintViolations.iterator().next();
		@SuppressWarnings("unchecked")
		HibernateConstraintViolation<Foo> hibernateConstraintViolation = constraintViolation.unwrap(HibernateConstraintViolation.class);

		Assert.assertEquals(INVALID_KEYWORDS, hibernateConstraintViolation.getInfo());
	}

	public class Foo {
		@OracleConstraint
		private final String question;

		public Foo(String question) {
			this.question = question;
		}
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = OracleConstraintImpl.class)
	public @interface OracleConstraint {
		String message() default "${formatter.format('the answer is: %1s', answer)}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class OracleConstraintImpl
			implements ConstraintValidator<OracleConstraint, String> {

		@Override
		public void initialize(OracleConstraint constraintAnnotation) {
		}

		@Override
		public boolean isValid(String question, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = context.unwrap( HibernateConstraintValidatorContext.class );


			if ( question.equals( QUESTION_1 ) ) {
				createSingleConstraintViolation( hibernateContext );
			}
			else if ( question.equals( QUESTION_2 ) ) {
				createMultipleConstraintViolationsUpdatingVariableValues( hibernateContext );
			}
			else if ( question.equals( QUESTION_3 ) ) {
				hibernateContext.addExpressionVariable( "answer", "${foo}" );
			}
			else if ( question.equals( QUESTION_4 ) ) {
				hibernateContext.withInfo(INVALID_KEYWORDS);
			}
			else {
				tryingToIllegallyUseNullAttributeName( hibernateContext );
			}

			// always return false to trigger constraint violation and message creation
			return false;
		}

		private void tryingToIllegallyUseNullAttributeName(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.addExpressionVariable( null, "foo" );
		}

		private void createMultipleConstraintViolationsUpdatingVariableValues(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.addExpressionVariable( "answer", 2 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 1: ${answer}" )
					.addConstraintViolation();

			// resetting the parameters
			hibernateContext.addExpressionVariable( "answer", 42 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 2: ${answer}" )
					.addConstraintViolation();
		}

		private void createSingleConstraintViolation(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.addExpressionVariable( "answer", 42 );
		}
	}
}


