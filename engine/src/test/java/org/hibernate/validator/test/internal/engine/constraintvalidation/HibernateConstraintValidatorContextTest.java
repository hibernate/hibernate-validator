/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class HibernateConstraintValidatorContextTest {

	private static final String QUESTION_1 = "The answer to life?";
	private static final String QUESTION_2 = "What is 1+1 and what is the answer to life?";
	private static final String QUESTION_3 = "This is a trick question";
	private static final String QUESTION_4 = "What keywords are not allowed?";
	private static final String QUESTION_5 = "What is 1+1 and what is the answer to life? But I won't get the right answer as Expression Language is disabled";

	private static final List<String> INVALID_KEYWORDS = Lists.newArrayList( "foo", "bar", "baz" );

	// Message parameters

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testSettingCustomMessageParameter() {
		Validator validator = getValidator();
		Set<ConstraintViolation<MessageParameterFoo>> constraintViolations = validator.validate( new MessageParameterFoo( QUESTION_1 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( MessageParameterOracleConstraint.class ).withMessage( "the answer is: 42" )
		);
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000028.*")
	@TestForIssue(jiraKey = "HV-701")
	public void testSettingInvalidCustomMessageParameter() {
		Validator validator = getValidator();
		validator.validate( new MessageParameterFoo( "" ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testCreatingMultipleConstraintViolationWithMessageParameters() {
		Validator validator = getValidator();
		Set<ConstraintViolation<MessageParameterFoo>> constraintViolations = validator.validate( new MessageParameterFoo( QUESTION_2 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( MessageParameterOracleConstraint.class ).withMessage( "answer 1: 2" ),
				violationOf( MessageParameterOracleConstraint.class ).withMessage( "answer 2: 42" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testExpressionTermAsMessageParameterValueIsTreatedAsString() {
		Validator validator = getValidator();
		Set<ConstraintViolation<MessageParameterFoo>> constraintViolations = validator.validate( new MessageParameterFoo( QUESTION_3 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( MessageParameterOracleConstraint.class ).withMessage( "the answer is: {foo}" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-951")
	public void testMessageParametersAreExposedInConstraintViolation() throws Exception {
		Validator validator = getValidator();
		Set<ConstraintViolation<MessageParameterFoo>> constraintViolations = validator.validate( new MessageParameterFoo( QUESTION_1 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( MessageParameterOracleConstraint.class ).withMessage( "the answer is: 42" )
		);

		ConstraintViolationImpl<MessageParameterFoo> constraintViolation = (ConstraintViolationImpl<MessageParameterFoo>) constraintViolations.iterator()
				.next();
		Map<String, Object> messageParameters = constraintViolation.getMessageParameters();
		Assert.assertEquals( messageParameters.size(), 1 );
		Assert.assertEquals( messageParameters.get( "answer" ), 42 );
	}

	// Expression variables

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testSettingCustomExpressionVariable() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_1 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class ).withMessage( "the answer is: 42" )
		);
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000028.*")
	@TestForIssue(jiraKey = "HV-701")
	public void testSettingInvalidCustomExpressionVariable() {
		Validator validator = getValidator();
		validator.validate( new ExpressionVariableFoo( "" ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testCreatingMultipleConstraintViolationWithExpressionVariablesWithExpressionLanguageEnabled() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_2 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class ).withMessage( "answer 1: 2" ),
				violationOf( ExpressionVariableOracleConstraint.class ).withMessage( "answer 2: 42" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-701")
	public void testExpressionTermAsExpressionVariableValueIsTreatedAsString() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_3 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class ).withMessage( "the answer is: ${foo}" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-951")
	public void testExpressionVariablesAreExposedInConstraintViolation() throws Exception {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_1 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class ).withMessage( "the answer is: 42" )
		);

		ConstraintViolationImpl<ExpressionVariableFoo> constraintViolation = (ConstraintViolationImpl<ExpressionVariableFoo>) constraintViolations.iterator()
				.next();
		Map<String, Object> expressionVariables = constraintViolation.getExpressionVariables();
		Assert.assertEquals( expressionVariables.size(), 1 );
		Assert.assertEquals( expressionVariables.get( "answer" ), 42 );
	}

	// Dynamic payload

	@Test
	@TestForIssue(jiraKey = "HV-1020")
	public void testDynamicPayloadExposedInHibernateConstraintViolation() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_4 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class )
		);

		ConstraintViolation<ExpressionVariableFoo> constraintViolation = constraintViolations.iterator().next();
		@SuppressWarnings("unchecked")
		HibernateConstraintViolation<ExpressionVariableFoo> hibernateConstraintViolation = constraintViolation.unwrap(
				HibernateConstraintViolation.class
		);

		Assert.assertEquals( hibernateConstraintViolation.getDynamicPayload( List.class ), INVALID_KEYWORDS );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1020")
	public void testNullIsReturnedForNonExistingPayloadType() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_4 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class )
		);

		ConstraintViolation<ExpressionVariableFoo> constraintViolation = constraintViolations.iterator().next();
		@SuppressWarnings("unchecked")
		HibernateConstraintViolation<ExpressionVariableFoo> hibernateConstraintViolation = constraintViolation.unwrap( HibernateConstraintViolation.class );

		Assert.assertNull( hibernateConstraintViolation.getDynamicPayload( String.class ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1164")
	public void testNullIsReturnedIfPayloadIsNull() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_1 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class )
		);

		ConstraintViolation<ExpressionVariableFoo> constraintViolation = constraintViolations.iterator().next();
		@SuppressWarnings("unchecked")
		HibernateConstraintViolation<ExpressionVariableFoo> hibernateConstraintViolation = constraintViolation.unwrap( HibernateConstraintViolation.class );

		Assert.assertNull( hibernateConstraintViolation.getDynamicPayload( Object.class ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1816")
	public void testCreatingMultipleConstraintViolationWithExpressionVariables() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ExpressionVariableFoo>> constraintViolations = validator.validate( new ExpressionVariableFoo( QUESTION_5 ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExpressionVariableOracleConstraint.class ).withMessage( "answer 1: ${answer}" ),
				violationOf( ExpressionVariableOracleConstraint.class ).withMessage( "answer 2: ${answer}" )
		);
	}

	public class MessageParameterFoo {
		@MessageParameterOracleConstraint
		private final String question;

		public MessageParameterFoo(String question) {
			this.question = question;
		}
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = MessageParameterOracleConstraintImpl.class)
	public @interface MessageParameterOracleConstraint {
		String message() default "the answer is: {answer}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class MessageParameterOracleConstraintImpl
			implements ConstraintValidator<MessageParameterOracleConstraint, String> {

		@Override
		public boolean isValid(String question, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = context.unwrap( HibernateConstraintValidatorContext.class );

			if ( question.equals( QUESTION_1 ) ) {
				createSingleConstraintViolation( hibernateContext );
			}
			else if ( question.equals( QUESTION_2 ) ) {
				createMultipleConstraintViolationsUpdatingMessageParameterValues( hibernateContext );
			}
			else if ( question.equals( QUESTION_3 ) ) {
				hibernateContext.addMessageParameter( "answer", "{foo}" );
			}
			else if ( question.equals( QUESTION_4 ) ) {
				hibernateContext.withDynamicPayload( INVALID_KEYWORDS );
			}
			else {
				tryingToIllegallyUseNullMessageParameterName( hibernateContext );
			}

			// always return false to trigger constraint violation and message creation
			return false;
		}

		private void tryingToIllegallyUseNullMessageParameterName(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.addMessageParameter( null, "foo" );
		}

		private void createMultipleConstraintViolationsUpdatingMessageParameterValues(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.addMessageParameter( "answer", 2 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 1: {answer}" )
					.addConstraintViolation();

			// resetting the message parameters
			hibernateContext.addMessageParameter( "answer", 42 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 2: {answer}" )
					.addConstraintViolation();
		}

		private void createSingleConstraintViolation(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.addMessageParameter( "answer", 42 );
		}
	}

	public class ExpressionVariableFoo {
		@ExpressionVariableOracleConstraint
		private final String question;

		public ExpressionVariableFoo(String question) {
			this.question = question;
		}
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = ExpressionVariableOracleConstraintImpl.class)
	public @interface ExpressionVariableOracleConstraint {
		String message() default "${formatter.format('the answer is: %1s', answer)}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ExpressionVariableOracleConstraintImpl
			implements ConstraintValidator<ExpressionVariableOracleConstraint, String> {

		@Override
		public boolean isValid(String question, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = context.unwrap( HibernateConstraintValidatorContext.class );

			if ( question.equals( QUESTION_1 ) ) {
				createSingleConstraintViolation( hibernateContext );
			}
			else if ( question.equals( QUESTION_2 ) ) {
				createMultipleConstraintViolationsUpdatingExpressionVariableValuesWithExpressionLanguageEnabled( hibernateContext );
			}
			else if ( question.equals( QUESTION_3 ) ) {
				hibernateContext.addExpressionVariable( "answer", "${foo}" );
			}
			else if ( question.equals( QUESTION_4 ) ) {
				hibernateContext.withDynamicPayload( INVALID_KEYWORDS );
			}
			else if ( question.equals( QUESTION_5 ) ) {
				createMultipleConstraintViolationsUpdatingExpressionVariableValues( hibernateContext );
			}
			else {
				tryingToIllegallyUseNullExpressionVariableName( hibernateContext );
			}

			// always return false to trigger constraint violation and message creation
			return false;
		}

		private void tryingToIllegallyUseNullExpressionVariableName(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.addMessageParameter( null, "foo" );
		}

		private void createMultipleConstraintViolationsUpdatingExpressionVariableValuesWithExpressionLanguageEnabled(
				HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.addExpressionVariable( "answer", 2 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 1: ${answer}" )
					.enableExpressionLanguage()
					.addConstraintViolation();

			// resetting the expression variables
			hibernateContext.addExpressionVariable( "answer", 42 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 2: ${answer}" )
					.enableExpressionLanguage()
					.addConstraintViolation();
		}

		private void createMultipleConstraintViolationsUpdatingExpressionVariableValues(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.addExpressionVariable( "answer", 2 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 1: ${answer}" )
					.addConstraintViolation();

			// resetting the expression variables
			hibernateContext.addExpressionVariable( "answer", 42 );
			hibernateContext.buildConstraintViolationWithTemplate( "answer 2: ${answer}" )
					.addConstraintViolation();
		}

		private void createSingleConstraintViolation(HibernateConstraintValidatorContext hibernateContext) {
			hibernateContext.addExpressionVariable( "answer", 42 );
		}
	}
}
