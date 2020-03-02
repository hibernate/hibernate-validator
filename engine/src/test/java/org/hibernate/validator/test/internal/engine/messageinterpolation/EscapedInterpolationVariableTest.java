/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.fail;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-798")
public class EscapedInterpolationVariableTest {
	private Validator validator;

	@BeforeTest
	public void setUp() {
		MessageInterpolator interpolator = new ResourceBundleMessageInterpolator(
				new ResourceBundleLocator() {
					@Override
					public ResourceBundle getResourceBundle(Locale locale) {
						return new ResourceBundle() {

							@Override
							protected Object handleGetObject(String key) {
								if ( "key-1".equals( key ) ) {
									return "\\{escapedParameterKey\\}";
								}
								else if ( "key-2".equals( key ) ) {
									// since {} are unbalanced the original key (key-2) should be returned from the interpolation
									return "{escapedParameterKey\\}";
								}
								else if ( "key-3".equals( key ) ) {
									// since {} are unbalanced the original key (key-3) should be returned from the interpolation
									return "\\{escapedParameterKey}";
								}
								else if ( "key-4".equals( key ) ) {
									return "foo";
								}
								else {
									fail( "Unexpected key: " + key );
								}
								return null;
							}

							@Override
							public Enumeration<String> getKeys() {
								throw new UnsupportedOperationException();
							}
						};
					}
				}, false
		);

		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration();
		ValidatorFactory factory = config.messageInterpolator( interpolator ).buildValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testEscapedOpeningAndClosingBrace() throws Exception {
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( new A() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class ).withMessage( "{escapedParameterKey}" )
		);
	}

	@Test
	public void testEscapedClosingBrace() throws Exception {
		Set<ConstraintViolation<B>> constraintViolations = validator.validate( new B() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class ).withMessage( "{key-2}" )
		);
	}

	@Test
	public void testEscapedOpenBrace() throws Exception {
		Set<ConstraintViolation<C>> constraintViolations = validator.validate( new C() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class ).withMessage( "{key-3}" )
		);
	}

	@Test
	public void testMessageStaysUnchangedDueToSingleCurlyBrace() throws Exception {
		Set<ConstraintViolation<D>> constraintViolations = validator.validate( new D() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class ).withMessage( "{key-4} {" )
		);
	}

	private class A {
		@Max(value = 1, message = "{key-1}")
		private int a = 2;
	}

	private class B {
		@Max(value = 1, message = "{key-2}")
		private int a = 2;
	}

	private class C {
		@Max(value = 1, message = "{key-3}")
		private int a = 2;
	}

	private class D {
		@Max(value = 1, message = "{key-4} {")
		private int a = 2;
	}
}


