/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util.annotationfactory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;

import org.junit.jupiter.api.Test;

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

		assertEquals( 5, size.min(), "Wrong parameter value" );
		assertEquals( 10, size.max(), "Wrong parameter value" );
	}

	@Test
	public void createAnnotationProxyMissingRequiredParameter() {
		assertThatThrownBy( () -> {
			AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
			descriptorBuilder.build().getAnnotation();
		} ).isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void createAnnotationProxyWithRequiredParameter() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", ".*" );

		Pattern pattern = descriptorBuilder.build().getAnnotation();

		assertEquals( pattern.regexp(), ".*", "Wrong parameter value" );
	}
}
