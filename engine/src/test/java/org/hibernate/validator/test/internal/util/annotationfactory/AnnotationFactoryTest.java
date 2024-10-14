/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util.annotationfactory;

import static org.testng.Assert.assertEquals;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class AnnotationFactoryTest {

	@Test
	public void createAnnotationProxy() {
		AnnotationDescriptor.Builder<Size> descriptorBuilder = new AnnotationDescriptor.Builder<>( Size.class );
		descriptorBuilder.setAttribute( "min", 5 );
		descriptorBuilder.setAttribute( "max", 10 );

		Size size = descriptorBuilder.build().getAnnotation();

		assertEquals( size.min(), 5, "Wrong parameter value" );
		assertEquals( size.max(), 10, "Wrong parameter value" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createAnnotationProxyMissingRequiredParameter() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.build().getAnnotation();
	}

	@Test
	public void createAnnotationProxyWithRequiredParameter() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", ".*" );

		Pattern pattern = descriptorBuilder.build().getAnnotation();

		assertEquals( ".*", pattern.regexp(), "Wrong parameter value" );
	}
}
