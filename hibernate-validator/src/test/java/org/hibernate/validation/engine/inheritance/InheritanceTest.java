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
package org.hibernate.validation.engine.inheritance;

import java.lang.annotation.Annotation;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.TestUtil;

/**
 * @author Hardy Ferentschik
 */
public class InheritanceTest {

	private static final Logger log = LoggerFactory.make();

	@Test
	public void testIsBeanConstrained() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Bar.class );

		assertFalse( beanDescriptor.hasConstraints(), "There should be no direct constraints on the specified bean." );
		assertTrue( beanDescriptor.isBeanConstrained(), "Bean should be constrainted  " );

		assertTrue( beanDescriptor.getConstraintsForProperty( "foo" ) != null );
		PropertyDescriptor propDescriptor = beanDescriptor.getConstraintsForProperty( "foo" );
		Annotation constraintAnnotation = (Annotation) propDescriptor.getConstraintDescriptors()
				.iterator()
				.next().getAnnotation();
		assertTrue(
				constraintAnnotation.annotationType() == NotNull.class
		);
	}
}
