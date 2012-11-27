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
package org.hibernate.validator.test.internal.util.annotationfactory;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

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
