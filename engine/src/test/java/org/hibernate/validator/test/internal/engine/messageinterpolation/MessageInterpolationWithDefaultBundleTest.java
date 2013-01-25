/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import java.util.Locale;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * Tests for correct message interpolation for messages from the default bundle.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class MessageInterpolationWithDefaultBundleTest {
	private Locale defaultLocale;

	@BeforeClass
	public void storeDefaultLocale() {
		defaultLocale = Locale.getDefault();
	}

	@AfterClass
	public void restoreDefaultLocale() {
		Locale.setDefault( defaultLocale );
	}

	@Test
	@TestForIssue(jiraKey = "HV-268")
	public void testEmailAndRangeMessageEnglishLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.ENGLISH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintViolationMessages(
				constraintViolations, "not a well-formed email address", "must be between 18 and 21"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-268")
	public void testEmailAndRangeMessageGermanLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.GERMAN );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintViolationMessages(
				constraintViolations, "keine g\u00FCltige E-Mail-Adresse", "muss zwischen 18 und 21 liegen"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-268")
	public void testEmailAndRangeMessageFrenchLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.FRENCH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintViolationMessages(
				constraintViolations, "Adresse email mal form\u00E9e", "doit \u00EAtre entre 18 et 21"
		);
	}

	/**
	 * HV-306. If English is explicitly set as locale for message interpolation, it
	 * must take precedence over the system's default locale.
	 */
	@Test
	@TestForIssue(jiraKey = "HV-306")
	public void testThatExplicitlySetEnglishLocaleHasPrecedenceOverDefaultLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.FRENCH );
		config.messageInterpolator( new LocalizedMessageInterpolator( Locale.ENGLISH ) );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintViolationMessages(
				constraintViolations, "not a well-formed email address", "must be between 18 and 21"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-256")
	public void testConditionalDecimalMinMessageDependingOnInclusiveFlag() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.ENGLISH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();


		Set<ConstraintViolation<DoubleHolder>> constraintViolations = validator.validate( new DoubleHolder() );
		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectConstraintViolationMessages(
				constraintViolations,
				"must be greater than or equal to 1.0",
				"must be greater than 1.0",
				"must be less than or equal to 1.0",
				"must be less than 1.0"
		);
	}

	private static class DoubleHolder {
		@DecimalMax(value = "1.0")
		private final double inclusiveMaxDouble;
		@DecimalMax(value = "1.0", inclusive = false)
		private final double exclusiveMaxDouble;

		@DecimalMin(value = "1.0")
		private final double inclusiveMinDouble;
		@DecimalMin(value = "1.0", inclusive = false)
		private final double exclusiveMinDouble;

		private DoubleHolder() {
			this.inclusiveMaxDouble = 1.1;
			this.exclusiveMaxDouble = 1.0;

			this.inclusiveMinDouble = 0.9;
			this.exclusiveMinDouble = 1.0;
		}
	}

	/**
	 * A message interpolator that enforces one given locale to be used for message
	 * interpolation.
	 *
	 * @author Gunnar Morling
	 */
	private static class LocalizedMessageInterpolator extends ResourceBundleMessageInterpolator {

		private Locale locale;

		public LocalizedMessageInterpolator(Locale locale) {
			this.locale = locale;
		}

		@Override
		public String interpolate(String messageTemplate, Context context) {
			return interpolate( messageTemplate, context, this.locale );
		}
	}
}
