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
package org.hibernate.validation.constraints.impl;

import javax.validation.constraints.Min;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validation.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validation.util.annotationfactory.AnnotationFactory;

/**
 * @author Alaa Nassef
 */
public class MinValidatorForStringTest {

	private static MinValidatorForString constraint;

	@BeforeClass
	public static void init() {
		AnnotationDescriptor<Min> descriptor = new AnnotationDescriptor<Min>( Min.class );
		descriptor.setValue( "value", 15l );
		descriptor.setValue( "message", "{validator.min}" );
		Min m = AnnotationFactory.create( descriptor );

		constraint = new MinValidatorForString();
		constraint.initialize( m );
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "20", null ) );
		assertTrue( constraint.isValid( "15", null ) );
		assertTrue( constraint.isValid( "15.0", null ) );
		assertFalse( constraint.isValid( "10", null ) );
		assertFalse( constraint.isValid( "14.99", null ) );
		assertFalse( constraint.isValid( "-14.99", null ) );
		//number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}

}
