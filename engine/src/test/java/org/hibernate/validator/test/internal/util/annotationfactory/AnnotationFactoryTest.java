/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.annotationfactory;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.testng.annotations.Test;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static org.testng.Assert.assertEquals;


/**
 * @author Hardy Ferentschik
 */
public class AnnotationFactoryTest {

	@Test
	public void createAnnotationProxy() {
		AnnotationDescriptor<Size> descriptor = new AnnotationDescriptor<Size>( Size.class );
		descriptor.setValue( "min", 5 );
		descriptor.setValue( "max", 10 );

		Size size = AnnotationFactory.create( descriptor );

		assertEquals( size.min(), 5, "Wrong parameter value" );
		assertEquals( size.max(), 10, "Wrong parameter value" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createAnnotationProxyMissingRequiredParameter() {
		AnnotationDescriptor<Pattern> descriptor = new AnnotationDescriptor<Pattern>( Pattern.class );
		AnnotationFactory.create( descriptor );
	}

	@Test
	public void createAnnotationProxyWithRequiredParameter() {
		AnnotationDescriptor<Pattern> descriptor = new AnnotationDescriptor<Pattern>( Pattern.class );
		descriptor.setValue( "regexp", ".*" );

		Pattern pattern = AnnotationFactory.create( descriptor );

		assertEquals( ".*", pattern.regexp(), "Wrong parameter value" );
	}
}
