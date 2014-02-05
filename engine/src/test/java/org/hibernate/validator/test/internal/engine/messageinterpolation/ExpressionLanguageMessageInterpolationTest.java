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

import java.util.Collections;
import java.util.Locale;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests for message interpolation using EL.
 *
 * @author Hardy Ferentschik
 */
public class ExpressionLanguageMessageInterpolationTest {

	private MessageInterpolator interpolatorUnderTest;
	private ConstraintDescriptorImpl<NotNull> notNullDescriptor;
	private ConstraintDescriptorImpl<Size> sizeDescriptor;

	@BeforeTest
	public void setUp() {
		// Create some annotations for testing using AnnotationProxies
		AnnotationDescriptor<NotNull> descriptor = new AnnotationDescriptor<NotNull>( NotNull.class );
		NotNull notNull = AnnotationFactory.create( descriptor );
		notNullDescriptor = new ConstraintDescriptorImpl<NotNull>(
				new ConstraintHelper(),
				null,
				notNull,
				java.lang.annotation.ElementType.FIELD
		);

		AnnotationDescriptor<Size> sizeAnnotationDescriptor = new AnnotationDescriptor<Size>( Size.class );
		Size size = AnnotationFactory.create( sizeAnnotationDescriptor );
		sizeDescriptor = new ConstraintDescriptorImpl<Size>(
				new ConstraintHelper(),
				null,
				size,
				java.lang.annotation.ElementType.FIELD
		);

		interpolatorUnderTest = new ResourceBundleMessageInterpolator();
	}

	@Test
	public void testExpressionLanguageGraphNavigation() {
		User user = new User();
		user.setAge( 18 );
		MessageInterpolator.Context context = new MessageInterpolatorContext(
				notNullDescriptor,
				user,
				null,
				Collections.<String, Object>emptyMap()
		);

		String expected = "18";
		String actual = interpolatorUnderTest.interpolate( "${validatedValue.age}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUnknownPropertyInExpressionLanguageGraphNavigation() {
		MessageInterpolator.Context context = new MessageInterpolatorContext(
				notNullDescriptor,
				new User(),
				null,
				Collections.<String, Object>emptyMap()
		);

		String expected = "${validatedValue.foo}";
		String actual = interpolatorUnderTest.interpolate( "${validatedValue.foo}", context );
		assertEquals( actual, expected, "No substitution should occur" );
	}

	@Test
	public void testNullValidatedValue() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "Validated value was null";
		String actual = interpolatorUnderTest.interpolate(
				"Validated value was ${validatedValue == null ? 'null' : validatedValue}",
				context
		);
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testExpressionAndParameterInterpolationInSameMessageDescriptor() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "2 0 2147483647";
		String actual = interpolatorUnderTest.interpolate( "${1+1} {min} {max}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testEscapedExpressionLanguage() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "${1+1}";
		String actual = interpolatorUnderTest.interpolate( "\\${1+1}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testTernaryExpressionLanguageOperator() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "foo";
		String actual = interpolatorUnderTest.interpolate( "${min == 0 ? 'foo' : 'bar'}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testParameterFormatting() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "Max 2147483647, min 0";
		String actual = interpolatorUnderTest.interpolate( "${formatter.format('Max %s, min %s', max, min)}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testLiteralStaysUnchanged() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "foo";
		String actual = interpolatorUnderTest.interpolate( "foo", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testLiteralBackslash() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "\\foo";
		String actual = interpolatorUnderTest.interpolate( "\\foo", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testPrecedenceOfParameterInterpolation() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "$0";
		String actual = interpolatorUnderTest.interpolate( "${min}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testLocaleBasedFormatting() {
		MessageInterpolator.Context context = new MessageInterpolatorContext(
				notNullDescriptor,
				42.00000d,
				null,
				Collections.<String, Object>emptyMap()
		);

		// german locale
		String expected = "42,00";
		String actual = interpolatorUnderTest.interpolate(
				"${formatter.format('%1$.2f', validatedValue)}",
				context,
				Locale.GERMAN
		);
		assertEquals( actual, expected, "Wrong substitution" );

		// us locale
		expected = "42.00";
		actual = interpolatorUnderTest.interpolate(
				"${formatter.format('%1$.2f', validatedValue)}",
				context,
				Locale.US
		);
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testMissingFormatArgument() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "${formatter.format('%1$s')}";
		String actual = interpolatorUnderTest.interpolate( "${formatter.format('%1$s')}", context );
		assertEquals(
				actual,
				expected,
				"Calling of formatter#format w/o format parameter. No substitution should occur"
		);
	}

	@Test
	public void testNoParametersToFormatter() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "${formatter.format()}";
		String actual = interpolatorUnderTest.interpolate( "${formatter.format()}", context );
		assertEquals( actual, expected, "Calling of formatter#format w/o parameters. No substitution should occur" );
	}

	@Test
	public void testNonFormatterFunction() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "foo";
		String actual = interpolatorUnderTest.interpolate( "${'foobar'.substring(0,3)}", context );
		assertEquals( actual, expected, "Calling of String#substring should work" );
	}

	@Test
	public void testCallingWrongFormatterMethod() {
		MessageInterpolator.Context context = new MessageInterpolatorContext(
				notNullDescriptor,
				42.00000d,
				null,
				Collections.<String, Object>emptyMap()
		);

		String expected = "${formatter.foo('%1$.2f', validatedValue)}";
		String actual = interpolatorUnderTest.interpolate(
				"${formatter.foo('%1$.2f', validatedValue)}",
				context,
				Locale.GERMAN
		);
		assertEquals(
				actual,
				expected,
				"Wrong substitution, no formatting should occur, because the wrong method name is used"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-834")
	public void testOpeningCurlyBraceInELExpression() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "{";
		String actual = interpolatorUnderTest.interpolate( "${1 > 0 ? '\\{' : '\\}'}", context );
		assertEquals( actual, expected, "Curly braces are allowed in EL expressions, but need to be escaped" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-834")
	public void testClosingCurlyBraceInELExpression() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "}";
		String actual = interpolatorUnderTest.interpolate( "${1 < 0 ? '\\{' : '\\}'}", context );
		assertEquals( actual, expected, "Curly braces are allowed in EL expressions, but need to be escaped" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-834")
	public void testCurlyBracesInELExpression() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "a{b}d";
		String actual = interpolatorUnderTest.interpolate( "${1 < 0 ? 'foo' : 'a\\{b\\}d'}", context );
		assertEquals( actual, expected, "Curly braces are allowed in EL expressions, but need to be escaped" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-834")
	public void testEscapedQuoteInELExpression() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "\"";
		String actual = interpolatorUnderTest.interpolate( "${ true ? \"\\\"\" : \"foo\"}", context );
		assertEquals( actual, expected, "If quotes are used in EL expression literal quotes need to be escaped" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-834")
	public void testSingleEscapedQuoteInELExpression() {
		MessageInterpolator.Context context = createMessageInterpolatorContext( sizeDescriptor );

		String expected = "'";
		String actual = interpolatorUnderTest.interpolate( "${ false ? 'foo' : '\\''}", context );
		assertEquals(
				actual,
				expected,
				"If single quotes are used in EL expression literal single quotes need to be escaped"
		);
	}

	private MessageInterpolatorContext createMessageInterpolatorContext(ConstraintDescriptorImpl<?> descriptor) {
		return new MessageInterpolatorContext(
				descriptor,
				null,
				null,
				Collections.<String, Object>emptyMap()
		);
	}
}
