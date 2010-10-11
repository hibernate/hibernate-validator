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
package org.hibernate.validator.test.constraints.impl;

import javax.validation.constraints.Digits;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.impl.DigitsValidatorForString;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

/**
 * @author Alaa Nassef
 */
public class DigitsValidatorForStringTest {

	private static DigitsValidatorForString constraint;

	@BeforeClass
	public static void init() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 5 );
		descriptor.setValue( "fraction", 2 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		constraint = new DigitsValidatorForString();
		constraint.initialize( p );
	}

	@Test
	public void testIsValid() {

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "0", null ) );
		assertTrue( constraint.isValid( "500.2", null ) );
		assertTrue( constraint.isValid( "-12456.22", null ) );
		assertTrue( constraint.isValid( "-000000000.22", null ) );
		//should throw number format exception
		assertFalse( constraint.isValid( "", null ) );
		assertFalse( constraint.isValid( "256874.0", null ) );
		assertFalse( constraint.isValid( "12.0001", null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeIntegerLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", -1 );
		descriptor.setValue( "fraction", 1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForString constraint = new DigitsValidatorForString();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 1 );
		descriptor.setValue( "fraction", -1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForString constraint = new DigitsValidatorForString();
		constraint.initialize( p );
	}
}
