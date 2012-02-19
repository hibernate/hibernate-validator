/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.internal.constraintvalidators.ScriptAssertValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link org.hibernate.validator.internal.constraintvalidators.ScriptAssertValidator}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ScriptAssertValidatorTest {

	@Test
	public void scriptEvaluatesToTrue() throws Exception {
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "groovy", "true" );

		assertTrue( validator.isValid( new Object(), null ) );
	}

	@Test
	public void scriptEvaluatesToFalse() throws Exception {
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "groovy", "false" );

		assertFalse( validator.isValid( new Object(), null ) );
	}

	@Test
	public void scriptExpressionReferencingAnnotatedObject() throws Exception {
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator(
				"groovy", "_this.startDate.before(_this.endDate)"
		);

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		assertTrue( validator.isValid( new CalendarEvent( startDate, endDate ), null ) );
		assertFalse( validator.isValid( new CalendarEvent( endDate, startDate ), null ) );
	}

	@Test
	public void scriptExpressionUsingCustomizedAlias() throws Exception {
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator(
				"groovy", "_.startDate.before(_.endDate)", "_"
		);

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		assertFalse( validator.isValid( new CalendarEvent( endDate, startDate ), null ) );
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
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "foo", "script" );

		validator.isValid( new Object(), null );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void illegalScriptExpressionRaisesException() throws Exception {
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "groovy", "foo" );

		validator.isValid( new Object(), null );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void scriptExpressionReturningNullRaisesException() throws Exception {
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "groovy", "null" );

		validator.isValid( new Object(), null );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void scriptExpressionReturningNoBooleanRaisesException() throws Exception {
		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator(
				"groovy", "new java.util.Date()"
		);

		validator.isValid( new Object(), null );
	}

	/**
	 * @param lang the script type
	 * @param script the actual script
	 * @param alias the alias name of the this object
	 *
	 * @return a {@link org.hibernate.validator.internal.constraintvalidators.ScriptAssertValidator} initialized with a {@link ScriptAssert} with the given values.
	 */
	private ConstraintValidator<ScriptAssert, Object> getInitializedValidator(String lang, String script, String alias) {

		ConstraintValidator<ScriptAssert, Object> validator = new ScriptAssertValidator();
		validator.initialize( getScriptAssert( lang, script, alias ) );

		return validator;
	}

	/**
	 * @param lang the script type
	 * @param script the actual script
	 *
	 * @return a {@link ScriptAssertValidator} initialized with a {@link ScriptAssert} with the given values.
	 */
	private ConstraintValidator<ScriptAssert, Object> getInitializedValidator(String lang, String script) {

		ConstraintValidator<ScriptAssert, Object> validator = new ScriptAssertValidator();
		validator.initialize( getScriptAssert( lang, script, null ) );

		return validator;
	}

	/**
	 * @param lang the script type
	 * @param script the actual script
	 * @param alias the alias name of the this object
	 *
	 * @return a {@link ScriptAssert} initialized with the given values.
	 */
	private ScriptAssert getScriptAssert(String lang, String script, String alias) {

		AnnotationDescriptor<ScriptAssert> descriptor = AnnotationDescriptor.getInstance( ScriptAssert.class );

		descriptor.setValue( "lang", lang );
		descriptor.setValue( "script", script );
		if ( alias != null ) {
			descriptor.setValue( "alias", alias );
		}

		return AnnotationFactory.create( descriptor );
	}

	/**
	 * An exemplary model class used in tests.
	 *
	 * @author Gunnar Morling
	 */
	private static class CalendarEvent {

		private Date startDate;

		private Date endDate;

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
}
