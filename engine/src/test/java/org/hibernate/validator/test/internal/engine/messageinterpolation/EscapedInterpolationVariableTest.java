/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.testng.AssertJUnit.fail;

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
		assertCorrectConstraintViolationMessages( constraintViolations, "{escapedParameterKey}" );
	}

	@Test
	public void testEscapedClosingBrace() throws Exception {
		Set<ConstraintViolation<B>> constraintViolations = validator.validate( new B() );
		assertCorrectConstraintViolationMessages( constraintViolations, "{key-2}" );
	}

	@Test
	public void testEscapedOpenBrace() throws Exception {
		Set<ConstraintViolation<C>> constraintViolations = validator.validate( new C() );
		assertCorrectConstraintViolationMessages( constraintViolations, "{key-3}" );
	}

	@Test
	public void testMessageStaysUnchangedDueToSingleCurlyBrace() throws Exception {
		Set<ConstraintViolation<D>> constraintViolations = validator.validate( new D() );
		assertCorrectConstraintViolationMessages( constraintViolations, "{key-4} {" );
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


