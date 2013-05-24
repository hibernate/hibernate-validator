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

import java.io.ByteArrayInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.validation.Configuration;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.testutil.TestForIssue;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class MessageInterpolationTest {
	private Validator validator;

	@BeforeClass
	public void createValidator() throws Exception {
		final StringBuilder lines = new StringBuilder();
		lines.append( "bar=Message is \\\\{escaped\\\\}" ).append( "\r\n" );
		lines.append( "baz=Message is US\\$ {value}" ).append( "\r\n" );
		lines.append( "buz=Message is {values}" ).append( "\r\n" );
		lines.append( "qux=Message is {missing}" ).append( "\r\n" );
		lines.append( "zap=Message is \\\\${value}" ).append( "\r\n" );
		lines.append( "escaped=wrong" ).append( "\r\n" );
		final ResourceBundle bundle = new PropertyResourceBundle(
				new ByteArrayInputStream( lines.toString().getBytes() )
		);
		Configuration<?> config = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator(
						new ResourceBundleMessageInterpolator(
								new ResourceBundleLocator() {

									@Override
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
	@TestForIssue(jiraKey = "HV-184")
	public void testCurlyBracesEscapingShouldBeRespected() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Bar.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is {escaped}" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-184")
	public void testAppendReplacementNeedsToEscapeBackslashAndDollarSign() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Baz.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is US$ 5" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-184")
	public void testUnknownParametersShouldBePreserved() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Qux.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is {missing}" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-506")
	public void testInterpolationOfArrayParameter() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Buz.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is [bar, baz, qux]" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-729")
	public void testDollarSignEscapingShouldBeRespected() {
		final ConstraintViolation<Foo> violation = validator.validate( new Foo(), Zap.class ).iterator().next();
		assertEquals( violation.getMessage(), "Message is $10" );
	}

	public interface Bar {
	}

	public interface Baz {
	}

	public interface Buz {
	}

	public interface Qux {
	}

	public interface Zap {
	}

	@Target(METHOD)
	@Retention(RUNTIME)
	@Constraint(validatedBy = AllowedValuesValidator.class)
	public static @interface AllowedValues {
		String[] values();

		String message() default "{buz}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {

		private List<String> values;

		@Override
		public void initialize(AllowedValues values) {
			this.values = Arrays.asList( values.values() );
		}

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			if ( value == null ) {
				return true;
			}
			return values.contains( value );
		}
	}

	public static class Foo {
		@NotNull(message = "{bar}", groups = Bar.class)
		public String getBar() {
			return null;
		}

		@Min(value = 5, message = "{baz}", groups = Baz.class)
		public int getBaz() {
			return 0;
		}

		@AllowedValues(values = { "bar", "baz", "qux" }, groups = Buz.class)
		public String getBuz() {
			return "buz";
		}

		@NotNull(message = "{qux}", groups = Qux.class)
		public String getQux() {
			return null;
		}

		@Min(value = 10, message = "{zap}", groups = Zap.class)
		public int getZap() {
			return 0;
		}
	}
}
