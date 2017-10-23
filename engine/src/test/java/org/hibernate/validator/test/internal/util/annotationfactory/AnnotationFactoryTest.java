/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.annotationfactory;

import static org.testng.Assert.assertEquals;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.testng.annotations.Test;


/**
 * @author Hardy Ferentschik
 */
public class AnnotationFactoryTest {

	@Test
	public void createAnnotationProxy() {
		AnnotationDescriptor.Builder<Size> descriptorBuilder = new AnnotationDescriptor.Builder<>( Size.class );
		descriptorBuilder.setValue( "min", 5 );
		descriptorBuilder.setValue( "max", 10 );

		Size size = descriptorBuilder.build().annotation();

		assertEquals( size.min(), 5, "Wrong parameter value" );
		assertEquals( size.max(), 10, "Wrong parameter value" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createAnnotationProxyMissingRequiredParameter() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.build().annotation();
	}

	@Test
	public void createAnnotationProxyWithRequiredParameter() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setValue( "regexp", ".*" );

		Pattern pattern = descriptorBuilder.build().annotation();

		assertEquals( ".*", pattern.regexp(), "Wrong parameter value" );
	}
}
