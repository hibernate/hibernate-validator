/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.initialize;
import static org.testng.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ScriptAssertValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Unit test for {@link org.hibernate.validator.internal.constraintvalidators.hv.ScriptAssertValidator}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ScriptAssertValidatorTest extends AbstractConstrainedTest {

	@Test
	public void scriptEvaluatesToTrue() throws Exception {
		@ScriptAssert(lang = "groovy", script = "true") class TmpType {
		}
		assertNoViolations( validator.validate( new TmpType() ) );

	}

	@Test
	public void scriptEvaluatesToFalse() throws Exception {
		@ScriptAssert(lang = "groovy", script = "false") class TmpType {
		}
		assertThat( validator.validate( new TmpType() ) ).containsOnlyViolations(
				violationOf( ScriptAssert.class )
		);
	}

	@Test
	public void scriptExpressionReferencingAnnotatedObject() throws Exception {
		@ScriptAssert(lang = "groovy", script = "_this.startDate.before(_this.endDate)") class TmpType extends CalendarEvent {
			public TmpType(Date startDate, Date endDate) {
				super( startDate, endDate );
			}
		}

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		assertNoViolations( validator.validate( new TmpType( startDate, endDate ) ) );
		assertThat( validator.validate( new TmpType( endDate, startDate ) ) ).containsOnlyViolations(
				violationOf( ScriptAssert.class )
		);
	}

	@Test
	public void scriptExpressionUsingCustomizedAlias() throws Exception {
		@ScriptAssert(lang = "groovy", script = "_.startDate.before(_.endDate)", alias = "_") class TmpType extends CalendarEvent {
			public TmpType(Date startDate, Date endDate) {
				super( startDate, endDate );
			}
		}

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		assertNoViolations( validator.validate( new TmpType( startDate, endDate ) ) );
		assertThat( validator.validate( new TmpType( endDate, startDate ) ) ).containsOnlyViolations(
				violationOf( ScriptAssert.class )
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void emptyLanguageNameRaisesException() throws Exception {
		getInitializedValidator( "", "script" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void emptyScriptRaisesException() throws Exception {
		getInitializedValidator( "lang", "" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void emptyAliasRaisesException() throws Exception {
		getInitializedValidator( "lang", "script", "" );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void unknownLanguageNameRaisesException() throws Exception {
		@ScriptAssert(lang = "foo", script = "script") class TmpType {
		}

		assertNoViolations( validator.validate( new TmpType() ) );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void illegalScriptExpressionRaisesException() throws Exception {
		@ScriptAssert(lang = "groovy", script = "foo") class TmpType {
		}

		assertNoViolations( validator.validate( new TmpType() ) );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void scriptExpressionReturningNullRaisesException() throws Exception {
		@ScriptAssert(lang = "groovy", script = "null") class TmpType {
		}

		assertNoViolations( validator.validate( new TmpType() ) );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void scriptExpressionReturningNoBooleanRaisesException() throws Exception {
		@ScriptAssert(lang = "groovy", script = "new java.util.Date()") class TmpType {
		}

		assertNoViolations( validator.validate( new TmpType() ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1201")
	public void reportOnField() {
		Validator validator = ValidatorUtil.getValidator();

		assertTrue( validator.validate( new AnnotatedCalendarEvent(
						Date.from( Instant.now() ),
						Date.from( Instant.now().plusMillis( 1000L ) )
				)
		).isEmpty(), "Should pass validation" );

		Set<ConstraintViolation<AnnotatedCalendarEvent>> fieldViolations = validator.validate(
				new AnnotatedCalendarEvent(
						Date.from( Instant.now().plusMillis( 1000L ) ),
						Date.from( Instant.now().minusMillis( 1000L ) )
				)
		);

		assertThat( fieldViolations ).containsOnlyViolations(
				violationOf( ScriptAssert.class )
						.withMessage( "script expression \"_this.startDate.before(_this.endDate)\" didn't evaluate to true" )
						.withPropertyPath( pathWith()
								.property( "startDate" )
						)
		);

		Set<ConstraintViolation<AnnotatedWithoutReportCalendarEvent>> beanViolations = validator.validate(
				new AnnotatedWithoutReportCalendarEvent(
						Date.from( Instant.now().plusMillis( 1000L ) ),
						Date.from( Instant.now().minusMillis( 1000L ) )
				)
		);

		assertThat( beanViolations ).containsOnlyViolations(
				violationOf( ScriptAssert.class )
						.withMessage( "script expression \"_this.startDate.before(_this.endDate)\" didn't evaluate to true" )
						.withPropertyPath( pathWith()
								.bean()
						)
		);
	}

	/**
	 * @param lang the script type
	 * @param script the actual script
	 * @param alias the alias name of the this object
	 *
	 * @return a {@link org.hibernate.validator.internal.constraintvalidators.hv.ScriptAssertValidator} initialized with a {@link ScriptAssert} with the given values.
	 */
	private ConstraintValidator<ScriptAssert, Object> getInitializedValidator(String lang, String script, String alias) {
		return getInitializedValidator( lang, script, alias, null );
	}

	/**
	 * @param lang the script type
	 * @param script the actual script
	 *
	 * @return a {@link ScriptAssertValidator} initialized with a {@link ScriptAssert} with the given values.
	 */
	private ConstraintValidator<ScriptAssert, Object> getInitializedValidator(String lang, String script) {
		return getInitializedValidator( lang, script, null );
	}

	/**
	 * @param lang the script type
	 * @param script the actual script
	 * @param alias the alias name of the this object
	 * @param reportOn a property name on which to report an error
	 *
	 * @return a {@link ScriptAssertValidator} initialized with a {@link ScriptAssert} with the given values.
	 */
	private ConstraintValidator<ScriptAssert, Object> getInitializedValidator(String lang, String script, String alias, String reportOn) {

		HibernateConstraintValidator<ScriptAssert, Object> validator = new ScriptAssertValidator();
		initialize( validator, getScriptAssert( lang, script, alias, reportOn ) );

		return validator;
	}

	/**
	 * @param lang the script type
	 * @param script the actual script
	 * @param alias the alias name of the this object
	 * @param reportOn a property name on which to report an error
	 *
	 * @return a {@link ScriptAssert} initialized with the given values.
	 */
	private ConstraintAnnotationDescriptor<ScriptAssert> getScriptAssert(String lang, String script, String alias, String reportOn) {
		ConstraintAnnotationDescriptor.Builder<ScriptAssert> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( ScriptAssert.class );

		descriptorBuilder.setAttribute( "lang", lang );
		descriptorBuilder.setAttribute( "script", script );
		if ( alias != null ) {
			descriptorBuilder.setAttribute( "alias", alias );
		}
		if ( reportOn != null ) {
			descriptorBuilder.setAttribute( "reportOn", reportOn );
		}

		return descriptorBuilder.build();
	}

	/**
	 * An exemplary model class used in tests.
	 *
	 * @author Gunnar Morling
	 */
	private static class CalendarEvent {

		private final Date startDate;

		private final Date endDate;

		public CalendarEvent(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@SuppressWarnings("unused")
		public Date getStartDate() {
			return startDate;
		}

		@SuppressWarnings("unused")
		public Date getEndDate() {
			return endDate;
		}
	}

	@ScriptAssert(lang = "groovy", script = "_this.startDate.before(_this.endDate)", reportOn = "startDate")
	@SuppressWarnings("unused")
	private static class AnnotatedCalendarEvent {

		private final Date startDate;
		private final Date endDate;

		public AnnotatedCalendarEvent(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}

	@ScriptAssert(lang = "groovy", script = "_this.startDate.before(_this.endDate)")
	@SuppressWarnings("unused")
	private static class AnnotatedWithoutReportCalendarEvent {

		private final Date startDate;
		private final Date endDate;

		public AnnotatedWithoutReportCalendarEvent(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}

}
