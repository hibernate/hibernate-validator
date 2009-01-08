// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.util.annotationfactory;

import javax.validation.constraints.Size;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.hibernate.validation.constraints.Pattern;

/**
 * @author Hardy Ferentschik
 */
public class AnnotationFactoryTest {

	@Test
	public void createAnnotationProxy() {
		AnnotationDescriptor descriptor = new AnnotationDescriptor( Size.class );
		descriptor.setValue( "min", 5 );
		descriptor.setValue( "max", 10 );

		Size size = AnnotationFactory.create( descriptor );

		assertEquals( "Wrong parameter value", 5, size.min() );
		assertEquals( "Wrong parameter value", 10, size.max() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void createAnnotationProxyMissingRequiredParamter() {
		AnnotationDescriptor descriptor = new AnnotationDescriptor( Pattern.class );
		Pattern pattern = AnnotationFactory.create( descriptor );
	}

	@Test
	public void createAnnotationProxyWithRequiredParamter() {
		AnnotationDescriptor descriptor = new AnnotationDescriptor( Pattern.class );
		descriptor.setValue( "regex", ".*" );

		Pattern pattern = AnnotationFactory.create( descriptor );

		assertEquals( "Wrong parameter value", ".*", pattern.regex() );
	}
}
