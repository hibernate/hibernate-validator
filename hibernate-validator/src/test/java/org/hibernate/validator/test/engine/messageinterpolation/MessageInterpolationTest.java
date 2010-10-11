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
package org.hibernate.validator.test.engine.messageinterpolation;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.ResourceBundleLocator;

import static org.testng.Assert.assertEquals;

/**
 * Tests for HV-184
 *
 * @author Hardy Ferentschik
 */
public class MessageInterpolationTest {
	private Validator validator;

	@BeforeClass
	public void createValidator() throws Exception {
		final StringBuilder lines = new StringBuilder();
		lines.append( "bar=Message is \\\\{escaped\\\\}" ).append( "\r\n" );
		lines.append( "baz=Message is US$ {value}" ).append( "\r\n" );
		lines.append( "qux=Message is {missing}" ).append( "\r\n" );
		lines.append( "escaped=wrong" ).append( "\r\n" );
		final ResourceBundle bundle = new PropertyResourceBundle(
				new ByteArrayInputStream( lines.toString().getBytes() )
		);
		Configuration<?> config = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator(
						new ResourceBundleMessageInterpolator(
								new ResourceBundleLocator() {

									public ResourceBundle getResourceBundle(
											Locale locale) {
										return bundle;
									}

								}
						)
				);

		ValidatorFactory factory = config.buildValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testCurlyBracesEscapingShouldBeRespected() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Bar.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is {escaped}" );
	}

	@Test
	public void testAppendReplacementNeedsToEscapeBackslashAndDollarSign() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Baz.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is US$ 5" );
	}

	@Test
	public void testUnknownParametersShouldBePreserved() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Qux.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is {missing}" );
	}

	public static interface Bar {
	}

	public static interface Baz {
	}

	public static interface Qux {
	}

	public static class Foo {
		@NotNull(message = "{bar}", groups = { Bar.class })
		public String getBar() {
			return null;
		}

		@Min(value = 5, message = "{baz}", groups = { Baz.class })
		public int getBaz() {
			return 0;
		}


		@NotNull(message = "{qux}", groups = { Qux.class })
		public String getQux() {
			return null;
		}
	}
}
