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

import java.util.Locale;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

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
				null,
				notNull,
				new ConstraintHelper(),
				java.lang.annotation.ElementType.FIELD,
				ConstraintOrigin.DEFINED_LOCALLY
		);

		AnnotationDescriptor<Size> sizeAnnotationDescriptor = new AnnotationDescriptor<Size>( Size.class );
		Size size = AnnotationFactory.create( sizeAnnotationDescriptor );
		sizeDescriptor = new ConstraintDescriptorImpl<Size>(
				null,
				size,
				new ConstraintHelper(),
				java.lang.annotation.ElementType.FIELD,
				ConstraintOrigin.DEFINED_LOCALLY
		);

		interpolatorUnderTest = new ResourceBundleMessageInterpolator();
	}

	@Test
	public void testExpressionLanguageGraphNavigation() {
		User user = new User();
		user.setAge( 18 );
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, user, null );

		String expected = "18";
		String actual = interpolatorUnderTest.interpolate( "${validatedValue.age}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUnknownPropertyInExpressionLanguageGraphNavigation() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, new User(), null );

		String expected = "${validatedValue.foo}";
		String actual = interpolatorUnderTest.interpolate( "${validatedValue.foo}", context );
		assertEquals( actual, expected, "No substitution should occur" );
	}

	@Test
	public void testNullValidatedValue() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null, null );

		String expected = "Validated value was null";
		String actual = interpolatorUnderTest.interpolate(
				"Validated value was ${validatedValue == null ? 'null' : validatedValue}",
				context
		);
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testExpressionAndParameterInterpolationInSameMessageDescriptor() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( sizeDescriptor, null, null );

		String expected = "2 0 2147483647";
		String actual = interpolatorUnderTest.interpolate( "${1+1} {min} {max}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testEscapedExpressionLanguage() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( sizeDescriptor, null, null );

		String expected = "${1+1}";
		String actual = interpolatorUnderTest.interpolate( "\\${1+1}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testTernaryExpressionLanguageOperator() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( sizeDescriptor, null, null );

		String expected = "foo";
		String actual = interpolatorUnderTest.interpolate( "${min == 0 ? 'foo' : 'bar'}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testParameterFormatting() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( sizeDescriptor, null, null );

		String expected = "Max 2147483647, min 0";
		String actual = interpolatorUnderTest.interpolate( "${formatter.format('Max %s, min %s', max, min)}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testLiteralStaysUnchanged() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( sizeDescriptor, null, null );

		String expected = "foo";
		String actual = interpolatorUnderTest.interpolate( "foo", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testLiteralBackslash() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( sizeDescriptor, null, null );

		String expected = "\\foo";
		String actual = interpolatorUnderTest.interpolate( "\\foo", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testPrecedenceOfParameterInterpolation() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( sizeDescriptor, null, null );

		String expected = "$0";
		String actual = interpolatorUnderTest.interpolate( "${min}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testLocaleBasedFormatting() {
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, 42.00000d, null );

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
}
